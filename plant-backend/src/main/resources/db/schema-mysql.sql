-- =================================================================
-- 植物护理管理系统的MySQL模式
-- 数据库：db_plant_official
-- =================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表结构
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  `deleted` tinyint(2) DEFAULT 0 COMMENT '逻辑删除: 0正常, 1删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- Table structure for official_plant
-- ----------------------------
DROP TABLE IF EXISTS `official_plant`;
CREATE TABLE `official_plant` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `genus` varchar(100) DEFAULT NULL COMMENT '属',
  `species` varchar(100) DEFAULT NULL COMMENT '种',
  `description` text COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='官方植物库';

-- ----------------------------
-- Table structure for audit_order
-- ----------------------------
DROP TABLE IF EXISTS `audit_order`;
CREATE TABLE `audit_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `local_plant_id` bigint(20) NOT NULL COMMENT '关联本地植物ID(SQLite)',
  `user_id` bigint(20) NOT NULL COMMENT '提交人ID',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/APPROVED/REJECTED',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核工单表';

-- ----------------------------
-- Table structure for care_schedule
-- ----------------------------
DROP TABLE IF EXISTS `care_schedule`;
CREATE TABLE `care_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `plant_id` bigint(20) NOT NULL COMMENT '植物ID',
  `plant_source` varchar(20) NOT NULL COMMENT '来源: OFFICIAL/LOCAL',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `due_time` datetime NOT NULL COMMENT '截止时间',
  `status` tinyint(2) DEFAULT 0 COMMENT '0:未完成, 1:已完成, 2:逾期',
  `reminder_config` text COMMENT '提醒配置JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='养护计划表';

-- ----------------------------
-- Table structure for care_record
-- ----------------------------
DROP TABLE IF EXISTS `care_record`;
CREATE TABLE `care_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `plant_id` bigint(20) NOT NULL COMMENT '植物ID',
  `plant_source` varchar(20) NOT NULL COMMENT '植物来源',
  `schedule_id` bigint(20) DEFAULT NULL COMMENT '关联计划ID',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `operations` text COMMENT '操作详情JSON',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='养护记录表';

-- ----------------------------
-- Table structure for plant_photo
-- ----------------------------
DROP TABLE IF EXISTS `plant_photo`;
CREATE TABLE `plant_photo` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `plant_id` bigint(20) NOT NULL COMMENT '植物ID',
  `plant_source` varchar(20) NOT NULL COMMENT '植物来源',
  `is_public` tinyint(1) DEFAULT 0 COMMENT '0:私有, 1:公开',
  `remarks` varchar(255) DEFAULT NULL COMMENT '备注',
  `url` varchar(255) NOT NULL COMMENT '访问URL',
  `file_path` varchar(255) DEFAULT NULL COMMENT '物理路径',
  `capture_time` datetime DEFAULT NULL COMMENT '拍摄时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_plant` (`user_id`, `plant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成长相册表';

-- ----------------------------
-- Table structure for reminder_config
-- ----------------------------
DROP TABLE IF EXISTS `reminder_config`;
CREATE TABLE `reminder_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `email` varchar(100) DEFAULT NULL COMMENT '通知邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '通知手机',
  `popup_enabled` tinyint(1) DEFAULT 1 COMMENT '弹窗提醒',
  `bell_enabled` tinyint(1) DEFAULT 1 COMMENT '铃声提醒',
  `scene_config` text COMMENT '场景配置JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒配置表';

-- ----------------------------
-- Table structure for reminder
-- ----------------------------
DROP TABLE IF EXISTS `reminder`;
CREATE TABLE `reminder` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `scene` varchar(50) NOT NULL COMMENT '场景: careSchedule/plantAudit...',
  `business_id` bigint(20) DEFAULT NULL COMMENT '业务ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `is_read` tinyint(1) DEFAULT 0 COMMENT '0:未读, 1:已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒通知表';

SET FOREIGN_KEY_CHECKS = 1;
