package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_order")
public class AuditOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long localPlantId; // 关联 SQLite ID
    private Long userId; // 提交人
    
    private String status; // PENDING (待审核), APPROVED (通过), REJECTED (驳回)
    private String rejectReason;
    
    private Long reviewerId;
    private LocalDateTime reviewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
