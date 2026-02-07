package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;
    private String password;
    private String email;
    private String phone;
    
    // 角色：ROLE_USER 或 ROLE_ADMIN
    private String role; 

    @TableLogic
    private Integer deleted; // 0: 正常, 1: 已删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
