package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.CareRecordDTO;
import com.plant.backend.entity.CareRecord;

import java.util.Map;

public interface CareRecordService extends IService<CareRecord> {
    CareRecord add(CareRecordDTO.AddRequest request);
    Map<String, Object> statistic(CareRecordDTO.StatQuery query, Long currentUserId);
}
