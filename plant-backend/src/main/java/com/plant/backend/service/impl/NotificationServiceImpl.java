package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plant.backend.entity.CareSchedule;
import com.plant.backend.entity.Reminder;
import com.plant.backend.entity.ReminderConfig;
import com.plant.backend.mapper.mysql.ReminderConfigMapper;
import com.plant.backend.mapper.mysql.ReminderMapper;
import com.plant.backend.service.EmailService;
import com.plant.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final ReminderMapper reminderMapper;
    private final ReminderConfigMapper configMapper;
    private final EmailService emailService;

    @Override
    public void sendCareReminderSummary(Long userId, List<CareSchedule> dueSchedules) {
        if (dueSchedules == null || dueSchedules.isEmpty())
            return;

        try {
            // 1. 生成站内信
            for (CareSchedule s : dueSchedules) {
                // 检查是否已存在提醒
                boolean exists = reminderMapper.exists(new LambdaQueryWrapper<Reminder>()
                        .eq(Reminder::getUserId, userId)
                        .eq(Reminder::getScene, "careSchedule")
                        .eq(Reminder::getBusinessId, s.getId()));

                if (!exists) {
                    Reminder r = new Reminder();
                    r.setUserId(userId);
                    r.setScene("careSchedule");
                    r.setBusinessId(s.getId());
                    r.setTitle("养护任务提醒");
                    r.setContent("任务 [" + s.getTaskName() + "] 今天需要完成。");
                    r.setIsRead(0);
                    reminderMapper.insert(r);
                }
            }

            // 2. 发送汇总邮件
            // 检查配置
            ReminderConfig config = configMapper.selectOne(new LambdaQueryWrapper<ReminderConfig>()
                    .eq(ReminderConfig::getUserId, userId));

            // 判断是否发送邮件 (popupEnabled? bellEnabled? 还是直接看Email字段? 假设只要有Email且未禁用则发)
            if (config != null && config.getEmail() != null && !config.getEmail().isEmpty()) {
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append("今日您有 ").append(dueSchedules.size()).append(" 项养护任务需要处理：\n");

                for (int i = 0; i < dueSchedules.size(); i++) {
                    CareSchedule s = dueSchedules.get(i);
                    contentBuilder.append(i + 1).append(". ")
                            .append(s.getTaskName())
                            .append(" (截止: ").append(s.getDueTime().toLocalDate()).append(")\n");
                }

                emailService.sendSimpleMail(config.getEmail(), "【Greenly】今日养护任务日报", contentBuilder.toString());
                log.info("Sent daily summary email to user {}", userId);
            }

        } catch (Exception e) {
            log.error("Failed to process notifications for user {}", userId, e);
        }
    }
}
