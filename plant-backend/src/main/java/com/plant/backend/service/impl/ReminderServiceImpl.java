package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plant.backend.dto.ReminderDTO;
import com.plant.backend.entity.Reminder;
import com.plant.backend.entity.ReminderConfig;
import com.plant.backend.mapper.mysql.ReminderMapper;
import com.plant.backend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl extends ServiceImpl<ReminderMapper, Reminder> implements ReminderService {

    private final com.plant.backend.mapper.mysql.ReminderConfigMapper reminderConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReminderConfig updateConfig(ReminderDTO.ConfigUpdateRequest request, Long userId) {
        LambdaQueryWrapper<ReminderConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReminderConfig::getUserId, userId);
        ReminderConfig config = reminderConfigMapper.selectOne(wrapper);

        if (config == null) {
            config = new ReminderConfig();
            config.setUserId(userId);
            config.setPopupEnabled(1); // 默认开启
            config.setBellEnabled(1);
        }

        if (request.getEmail() != null)
            config.setEmail(request.getEmail());
        if (request.getPhone() != null)
            config.setPhone(request.getPhone());
        if (request.getPopupEnabled() != null)
            config.setPopupEnabled(request.getPopupEnabled());
        if (request.getBellEnabled() != null)
            config.setBellEnabled(request.getBellEnabled());
        if (request.getSceneConfig() != null)
            config.setSceneConfig(request.getSceneConfig());

        if (config.getId() == null) {
            reminderConfigMapper.insert(config);
        } else {
            reminderConfigMapper.updateById(config);
        }

        return config;
    }

    @Override
    public ReminderDTO.UnreadResponse getUnread(Long userId) {
        // 查找未读提醒
        List<Reminder> unreadList = list(new LambdaQueryWrapper<Reminder>()
                .eq(Reminder::getUserId, userId)
                .eq(Reminder::getIsRead, 0)
                .orderByDesc(Reminder::getCreateTime));

        ReminderDTO.UnreadResponse response = new ReminderDTO.UnreadResponse();
        response.setTotalUnread((long) unreadList.size());

        // 按场景分组
        Map<String, List<Object>> details = new HashMap<>();
        // 初始化键
        details.put("careSchedule", new ArrayList<>());
        details.put("plantAudit", new ArrayList<>());
        details.put("announcement", new ArrayList<>());

        for (Reminder r : unreadList) {
            String scene = r.getScene();
            details.computeIfAbsent(scene, k -> new ArrayList<>()).add(r);
        }

        response.setDetails(details);
        response.setDetails(details);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id, Long userId) {
        Reminder reminder = getById(id);
        if (reminder == null) {
            // Silently fail or throw? Ideally throw if strictly managing state, but for UI
            // actions silent is often okay.
            // Let's be strict.
            throw new com.plant.backend.exception.BusinessException(
                    com.plant.backend.util.ResultCode.NOT_FOUND.getCode(), "提醒不存在");
        }
        if (!reminder.getUserId().equals(userId)) {
            throw new com.plant.backend.exception.BusinessException(com.plant.backend.util.ResultCode.FORBIDDEN);
        }

        reminder.setIsRead(1);
        updateById(reminder);
    }
}
