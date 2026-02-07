package com.plant.backend.mapper.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plant.backend.entity.AuditOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditOrderMapper extends BaseMapper<AuditOrder> {
}
