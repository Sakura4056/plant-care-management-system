package com.plant.backend.mapper.sqlite;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plant.backend.entity.LocalPlant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LocalPlantMapper extends BaseMapper<LocalPlant> {
}
