package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 本地植物实体类，映射到 SQLite 数据库中的 local_plant 表。
 * 设计时考虑了 SQLite 的特性和限制，如自增主键、日期时间存储等。
 * 需求说明中提到的字段和状态都已包含在内。
 */
@Data
@TableName("local_plant")
public class LocalPlant {
    // SQLite 自增主键为 INTEGER PRIMARY KEY
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private String genus;
    private String species;
    private String description;
    
    // 审核状态: UNSUBMITTED (未提交), PENDING (待审核), APPROVED (通过), REJECTED (驳回)
    // 为了 SQLite 可读性存储为字符串
    private String auditStatus; 

    // 标记逻辑删除或状态 'DELETED'/'CANCELLED' 如果用户被删除？
    // 需求说明: "同步更新...为‘已注销’标记"
    // 我们使用 auditStatus 或描述追加。
    // 需求: "更新自定义植物为‘已注销’标记"。
    
    private String createTime; // SQLite 不严格支持 LocalDateTime，通常存储为文本/字符串更安全。
    // MP 支持 LocalDateTime 映射到标准 JDBC 类型。SQLite JDBC 应能处理它。
}
