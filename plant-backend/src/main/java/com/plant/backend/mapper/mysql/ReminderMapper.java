package com.plant.backend.mapper.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plant.backend.entity.Reminder;
import com.plant.backend.entity.ReminderConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReminderMapper extends BaseMapper<Reminder> {
}


