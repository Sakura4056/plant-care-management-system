package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.PlantDTO;
import com.plant.backend.entity.AuditOrder;
import com.plant.backend.entity.LocalPlant;
import com.plant.backend.entity.OfficialPlant;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.mysql.AuditOrderMapper;
import com.plant.backend.mapper.mysql.OfficialPlantMapper;
import com.plant.backend.mapper.sqlite.LocalPlantMapper;
import com.plant.backend.service.PlantService;
import com.plant.backend.util.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PlantServiceImpl implements PlantService {

    private final OfficialPlantMapper officialPlantMapper;
    private final LocalPlantMapper localPlantMapper;
    private final AuditOrderMapper auditOrderMapper;

    @Override
    public Page<OfficialPlant> queryOfficial(PlantDTO.OfficialQuery query) {
        Page<OfficialPlant> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<OfficialPlant> wrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(OfficialPlant::getName, query.getKeyword())
                    .or().like(OfficialPlant::getGenus, query.getKeyword())
                    .or().like(OfficialPlant::getSpecies, query.getKeyword());
        }

        return officialPlantMapper.selectPage(page, wrapper);
    }

    @Override
    public java.util.List<LocalPlant> queryLocal(Long userId, String keyword) {
        LambdaQueryWrapper<LocalPlant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocalPlant::getUserId, userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(LocalPlant::getName, keyword)
                    .or().like(LocalPlant::getGenus, keyword)
                    .or().like(LocalPlant::getSpecies, keyword));
        }

        wrapper.orderByDesc(LocalPlant::getId);
        return localPlantMapper.selectList(wrapper);
    }

    @Override
    public LocalPlant addLocal(PlantDTO.LocalAddRequest request) {
        LocalPlant localPlant = new LocalPlant();
        localPlant.setUserId(request.getUserId());
        localPlant.setName(request.getName());
        localPlant.setGenus(request.getGenus());
        localPlant.setSpecies(request.getSpecies());
        localPlant.setDescription(request.getDescription());
        localPlant.setAuditStatus("LOCAL");
        localPlant.setCreateTime(LocalDateTime.now().toString()); // SQLite 文本存储

        localPlantMapper.insert(localPlant);
        return localPlant;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLocal(Long localPlantId, Long userId) {
        LocalPlant localPlant = localPlantMapper.selectById(localPlantId);
        if (localPlant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "植物不存在");
        }
        if (!localPlant.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 1. 删除SQLite中的植物
        localPlantMapper.deleteById(localPlantId);

        // 2. 级联删除相关的、未完成的养护计划 (MySQL)
        // 引用其他 Mapper? 可以注入 CareScheduleMapper
        // 但为了避免循环依赖，也许最好通过 Event 或者直接注入 (Service impl 是可以的)
        // 让我们看看是否已有 CareScheduleMapper 注入? 还没有。我们需要注入它。
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 注意：事务通常默认只对主库 (MySQL) 生效，除非使用 ChainedTransactionManager。
    // 对于双数据源且没有 XA/ChainedTM，@Transactional 通常绑定到 Primary。
    // 这里的 SQLite 操作相对于 MySQL 事务通常是“非事务性的”或自动提交的。
    // 我们会先做 SQLite 更新 (如果失败最安全，MySQL 不会执行)。
    // 如果 MySQL 失败，SQLite 变更已持久化 (数据不一致)。
    // 严格方案需要 JTA。在此范围内，采用“尽力而为”的顺序：
    // 1. 检查约束。
    // 2. MySQL 插入 (在逻辑中)。
    // 3. SQLite 更新。
    // 使用手动排序。
    public AuditOrder submitAudit(Long localPlantId, Long userId) {
        LocalPlant localPlant = localPlantMapper.selectById(localPlantId);
        if (localPlant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "植物不存在");
        }
        if (!localPlant.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (!"LOCAL".equals(localPlant.getAuditStatus()) && !"UNSUBMITTED".equals(localPlant.getAuditStatus())
                && !"REJECTED".equals(localPlant.getAuditStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "当前状态不可提交审核");
        }

        // MySQL 中创建工单 (事务性)
        AuditOrder order = new AuditOrder();
        order.setLocalPlantId(localPlantId);
        order.setUserId(userId);
        order.setStatus("PENDING");

        auditOrderMapper.insert(order);

        // 更新 SQLite (通常自动提交)
        localPlant.setAuditStatus("PENDING");
        localPlantMapper.updateById(localPlant);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long auditOrderId, PlantDTO.AuditRequest request) {
        AuditOrder order = auditOrderMapper.selectById(auditOrderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "审核工单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "工单已处理");
        }

        String status = request.getStatus(); // APPROVED / REJECTED

        // 1. 更新 MySQL 工单
        order.setStatus(status);
        order.setReviewerId(request.getReviewerId());
        order.setReviewTime(LocalDateTime.now());
        if ("REJECTED".equals(status)) {
            if (request.getRejectReason() == null)
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "驳回原因必填");
            order.setRejectReason(request.getRejectReason());
        }
        auditOrderMapper.updateById(order);

        // 2. 更新 SQLite 植物状态
        LocalPlant localPlant = localPlantMapper.selectById(order.getLocalPlantId());
        if (localPlant != null) {
            localPlant.setAuditStatus(status);
            localPlantMapper.updateById(localPlant);

            // 3. 如果通过，同步到官方植物 (MySQL)
            if ("APPROVED".equals(status)) {
                OfficialPlant official = new OfficialPlant();
                official.setName(localPlant.getName());
                official.setGenus(localPlant.getGenus());
                official.setSpecies(localPlant.getSpecies());
                official.setDescription(localPlant.getDescription());
                officialPlantMapper.insert(official);
            }
        }
    }

    @Override
    public Page<AuditOrder> queryAuditOrders(Page<AuditOrder> page, String status) {
        LambdaQueryWrapper<AuditOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(AuditOrder::getStatus, status);
        }
        wrapper.orderByDesc(AuditOrder::getCreateTime);
        return auditOrderMapper.selectPage(page, wrapper);
    }

    @org.springframework.context.event.EventListener
    public void handleUserDelete(com.plant.backend.event.UserDeletedEvent event) {
        Long userId = event.getUserId();
        // SQLite 同步删除: 更新状态为 "已注销" 或删除

        LambdaUpdateWrapper<LocalPlant> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LocalPlant::getUserId, userId)
                .set(LocalPlant::getAuditStatus, "已注销");

        localPlantMapper.update(null, updateWrapper);
    }
}
