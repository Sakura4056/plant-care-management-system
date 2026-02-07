package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plant.backend.dto.CareRecordDTO;
import com.plant.backend.entity.CareRecord;
import com.plant.backend.entity.CareSchedule;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.mysql.CareRecordMapper;
import com.plant.backend.mapper.mysql.CareScheduleMapper;
import com.plant.backend.service.CareRecordService;
import com.plant.backend.util.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareRecordServiceImpl extends ServiceImpl<CareRecordMapper, CareRecord> implements CareRecordService {

    private final CareScheduleMapper careScheduleMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CareRecord add(CareRecordDTO.AddRequest request) {
        CareRecord record = new CareRecord();
        record.setUserId(request.getUserId());
        record.setPlantId(request.getPlantId());
        record.setPlantSource(request.getPlantSource());
        record.setScheduleId(request.getScheduleId());
        record.setRecordTime(request.getRecordTime());
        record.setOperations(request.getOperations());
        record.setRemarks(request.getRemarks());

        save(record);

        // 如果关联了计划，标记为已完成？
        if (request.getScheduleId() != null) {
            CareSchedule schedule = careScheduleMapper.selectById(request.getScheduleId());
            if (schedule != null && schedule.getUserId().equals(request.getUserId())) {
                schedule.setStatus(1); // 已完成
                careScheduleMapper.updateById(schedule);
            }
        }

        return record;
    }

    @Override
    public Map<String, Object> statistic(CareRecordDTO.StatQuery query, Long currentUserId) {
        if (query.getUserId() != null && !query.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        int days = (query.getDays() != null && query.getDays() == 90) ? 90 : 30;
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<CareRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareRecord::getUserId, currentUserId) // 统计通常是针对用户的
                .ge(CareRecord::getRecordTime, startTime);

        if (query.getPlantId() != null) {
            wrapper.eq(CareRecord::getPlantId, query.getPlantId());
            if (query.getPlantSource() != null) {
                wrapper.eq(CareRecord::getPlantSource, query.getPlantSource());
            }
        }

        List<CareRecord> records = list(wrapper); // 恢复 records 变量定义

        // 聚合
        // 格式: Context Date -> { water: x, fertilizer: y, count: z }
        Map<String, Map<String, Double>> aggMap = new TreeMap<>(); // Sorted by date

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (CareRecord r : records) {
            String dateStr = r.getRecordTime().format(formatter);
            aggMap.putIfAbsent(dateStr, new HashMap<>());
            Map<String, Double> dayStats = aggMap.get(dateStr);

            dayStats.merge("count", 1.0, (a, b) -> a + b);

            // 解析 JSON 操作
            try {
                JsonNode root = objectMapper.readTree(r.getOperations());
                if (root.has("water")) {
                    dayStats.merge("water", root.get("water").asDouble(), (a, b) -> a + b);
                }
                if (root.has("fertilizer")) {
                    dayStats.merge("fertilizer", root.get("fertilizer").asDouble(), (a, b) -> a + b);
                }
            } catch (JsonProcessingException e) {
                log.warn("解析记录{}的操作JSON失败", r.getId());
            }
        }

        // 转换为数组用于图表
        List<String> dates = new ArrayList<>(aggMap.keySet());
        List<Double> waterData = new ArrayList<>();
        List<Double> fertilizerData = new ArrayList<>();
        List<Double> countData = new ArrayList<>();

        for (String date : dates) {
            Map<String, Double> day = aggMap.get(date);
            waterData.add(day.getOrDefault("water", 0.0));
            fertilizerData.add(day.getOrDefault("fertilizer", 0.0));
            countData.add(day.getOrDefault("count", 0.0));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("water", waterData);
        result.put("fertilizer", fertilizerData);
        result.put("totalRecords", countData);

        return result;
    }
}
