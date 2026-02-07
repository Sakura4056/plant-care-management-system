package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("official_plant")
public class OfficialPlant {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String genus; // 属
    private String species; // 种
    private String description;
    
    // 可选：图片 URL 等。需求未明确列出查询参数以外的字段，
    // 但隐含了名称、属、种。为了完整性添加描述字段。

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
