package com.plant.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plant.backend.entity.CareSchedule;

import com.plant.backend.mapper.mysql.CareScheduleMapper;
import com.plant.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CareTask {

    private final CareScheduleMapper careScheduleMapper;
    private final NotificationService notificationService;

    // ... (保留scanDueSchedules方法头) ...
    @Scheduled(cron = "0 0 9 * * ?")
    public void scanDueSchedules() {
        log.info("正在扫描到期的养护计划（每日汇总）...");

        // 查找截止时间 <= 今天结束 (23:59:59) 且未完成的任务
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        LambdaQueryWrapper<CareSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareSchedule::getStatus, 0)
                .le(CareSchedule::getDueTime, todayEnd);

        List<CareSchedule> dueList = careScheduleMapper.selectList(wrapper);

        if (dueList.isEmpty()) {
            return;
        }

        // 按用户分组
        java.util.Map<Long, List<CareSchedule>> userMap = dueList.stream()
                .collect(java.util.stream.Collectors.groupingBy(CareSchedule::getUserId));

        // 遍历每个用户发送汇总
        userMap.forEach((userId, schedules) -> {
            notificationService.sendCareReminderSummary(userId, schedules);
        });
    }
}
