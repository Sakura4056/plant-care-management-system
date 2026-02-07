# 数据库设计文档

## 概述

植物养护管理系统采用**MySQL + SQLite双数据源架构**：

- **MySQL**：存储系统核心数据（用户、官方植物、养护计划等）
- **SQLite**：存储用户本地自定义植物数据

## 数据模型

### E-R图概览

```
┌─────────────┐       ┌─────────────────┐
│   sys_user  │       │  official_plant │
│   (用户表)   │       │   (官方植物表)   │
└──────┬──────┘       └────────┬────────┘
       │                       │
       │ 1:N                   │ 1:N
       ▼                       ▼
┌──────────────┐      ┌───────────────┐
│ local_plant  │      │ care_schedule │
│ (本地植物表) │      │  (养护计划表)  │
└──────┬───────┘      └───────┬───────┘
       │                      │
       │ 1:N                  │ 1:N
       ▼                      ▼
┌──────────────┐      ┌──────────────┐
│  audit_order │      │  care_record │
│  (审核工单)   │      │  (养护记录)   │
└──────────────┘      └──────────────┘

┌──────────────┐      ┌─────────────────┐
│  plant_photo │      │    reminder     │
│   (照片表)    │      │    (提醒表)     │
└──────────────┘      └─────────────────┘
```

## MySQL数据表

### 1. sys_user（用户表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| user_id | bigint(20) | PK, AUTO_INCREMENT | 用户ID |
| username | varchar(50) | NOT NULL, UNIQUE | 用户名 |
| password | varchar(100) | NOT NULL | 加密密码 |
| email | varchar(100) | NULL | 邮箱 |
| phone | varchar(20) | NULL | 手机号 |
| role | varchar(20) | DEFAULT 'USER' | 角色：USER/ADMIN |
| deleted | tinyint(2) | DEFAULT 0 | 逻辑删除：0正常，1删除 |
| create_time | datetime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | datetime | NULL | 更新时间 |

**索引：**
- PRIMARY KEY: user_id
- UNIQUE KEY: uk_username (username)

---

### 2. official_plant（官方植物表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | bigint(20) | PK, AUTO_INCREMENT | 主键ID |
| name | varchar(100) | NOT NULL | 植物名称 |
| genus | varchar(100) | NULL | 属 |
| species | varchar(100) | NULL | 种 |
| description | text | NULL | 描述 |
| care_guide | text | NULL | 养护指南 |
| image_url | varchar(500) | NULL | 图片URL |
| water_frequency | int(11) | NULL | 浇水频率（天） |
| sunlight | varchar(50) | NULL | 光照需求 |
| temperature | varchar(50) | NULL | 适宜温度 |
| humidity | varchar(50) | NULL | 湿度要求 |
| fertilizer | varchar(200) | NULL | 施肥建议 |
| common_problems | text | NULL | 常见问题 |
| created_by | bigint(20) | NULL | 创建者ID |
| created_time | datetime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_time | datetime | NULL | 更新时间 |

**索引：**
- PRIMARY KEY: id
- KEY idx_name (name)
- KEY idx_genus (genus)

---

### 3. audit_order（审核工单表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | bigint(20) | PK, AUTO_INCREMENT | 工单ID |
| local_plant_id | bigint(20) | NOT NULL | 本地植物ID |
| user_id | bigint(20) | NOT NULL | 提交用户ID |
| status | varchar(20) | DEFAULT 'PENDING' | 状态：PENDING/APPROVED/REJECTED |
| reject_reason | varchar(500) | NULL | 拒绝原因 |
| reviewer_id | bigint(20) | NULL | 审核人ID |
| review_time | datetime | NULL | 审核时间 |
| create_time | datetime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY: id
- KEY idx_user_id (user_id)
- KEY idx_status (status)

---

### 4. care_schedule（养护计划表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | bigint(20) | PK, AUTO_INCREMENT | 计划ID |
| user_id | bigint(20) | NOT NULL | 用户ID |
| plant_id | bigint(20) | NOT NULL | 植物ID |
| plant_source | varchar(20) | NOT NULL | 植物来源：LOCAL/OFFICIAL |
| task_name | varchar(100) | NOT NULL | 任务名称 |
| due_time | datetime | NOT NULL | 截止时间 |
| status | tinyint(4) | DEFAULT 0 | 状态：0未完成，1已完成，2逾期 |
| reminder_config | json | NULL | 提醒配置 |
| create_time | datetime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | datetime | NULL | 更新时间 |

**索引：**
- PRIMARY KEY: id
- KEY idx_user_id (user_id)
- KEY idx_plant (plant_id, plant_source)
- KEY idx_status (status)
- KEY idx_due_time (due_time)

---

### 5. care_record（养护记录表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | bigint(20) | PK, AUTO_INCREMENT | 记录ID |
| user_id | bigint(20) | NOT NULL | 用户ID |
| plant_id | bigint(20) | NOT NULL | 植物ID |
| plant_source | varchar(20) | NOT NULL | 植物来源 |
| schedule_id | bigint(20) | NULL | 关联计划ID |
| record_time | datetime | NOT NULL | 记录时间 |
| operations | json | NOT NULL | 操作详情（JSON） |
| remarks | text | NULL | 备注 |
| create_time | datetime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY: id
- KEY idx_user_id (user_id)
- KEY idx_plant (plant_id, plant_source)
- KEY idx_record_time (record_time)

---

### 6. plant_photo（植物照片表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | bigint(20) | PK, AUTO_INCREMENT | 照片ID |
| user_id | bigint(20) | NOT NULL | 用户ID |
| plant_id | bigint(20) | NOT NULL | 植物ID |
| plant_source | varchar(20) | NOT NULL | 植物来源 |
| url | varchar(500) | NOT NULL | 照片URL |
| capture_time | datetime | NULL | 拍摄时间 |
| location | varchar(200) | NULL | 拍摄地点 |
| exif_info | json | NULL | EXIF信息 |
| is_public | tinyint(4) | DEFAULT 0 | 是否公开：0私有，1公开 |
| remarks | text | NULL | 备注 |
| create_time | datetime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | datetime | NULL | 更新时间 |

**索引：**
- PRIMARY KEY: id
- KEY idx_user_id (user_id)
- KEY idx_plant (plant_id, plant_source)
- KEY idx_capture_time (capture_time)

---

### 7. reminder（提醒通知表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | bigint(20) | PK, AUTO_INCREMENT | 提醒ID |
| user_id | bigint(20) | NOT NULL | 用户ID |
| type | varchar(50) | NOT NULL | 提醒类型 |
| title | varchar(200) | NOT NULL | 标题 |
| content | text | NULL | 内容 |
| related_id | bigint(20) | NULL | 关联ID |
| is_read | tinyint(4) | DEFAULT 0 | 是否已读：0未读，1已读 |
| read_time | datetime | NULL | 读取时间 |
| create_time | datetime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：**
- PRIMARY KEY: id
- KEY idx_user_id (user_id)
- KEY idx_user_read (user_id, is_read)
- KEY idx_create_time (create_time)

---

### 8. reminder_config（提醒配置表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | bigint(20) | PK, AUTO_INCREMENT | 配置ID |
| user_id | bigint(20) | NOT NULL, UNIQUE | 用户ID |
| email | varchar(100) | NULL | 邮箱地址 |
| popup_enabled | tinyint(4) | DEFAULT 1 | 弹窗提醒：0关闭，1开启 |
| bell_enabled | tinyint(4) | DEFAULT 1 | 铃声提醒：0关闭，1开启 |
| scene_config | json | NULL | 场景配置 |
| create_time | datetime | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | datetime | NULL | 更新时间 |

**索引：**
- PRIMARY KEY: id
- UNIQUE KEY: uk_user_id (user_id)

---

## SQLite数据表

### local_plant（本地植物表）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INTEGER | PK, AUTOINCREMENT | 主键ID |
| user_id | INTEGER | NOT NULL | 用户ID |
| name | TEXT | NOT NULL | 植物名称 |
| genus | TEXT | NULL | 属 |
| species | TEXT | NULL | 种 |
| description | TEXT | NULL | 描述 |
| audit_status | TEXT | DEFAULT 'LOCAL' | 审核状态：LOCAL/UNSUBMITTED/PENDING/APPROVED/REJECTED |
| create_time | TEXT | NULL | 创建时间 |

**索引：**
- PRIMARY KEY: id
- INDEX idx_user_id (user_id)

---

## 数据字典

### 枚举值说明

#### 用户角色 (role)
| 值 | 说明 |
|----|------|
| USER | 普通用户 |
| ADMIN | 管理员 |

#### 审核状态 (status/audit_status)
| 值 | 说明 |
|----|------|
| LOCAL | 本地植物（未提交审核） |
| UNSUBMITTED | 未提交 |
| PENDING | 待审核 |
| APPROVED | 已通过 |
| REJECTED | 已拒绝 |

#### 计划状态 (status)
| 值 | 说明 |
|----|------|
| 0 | 未完成 |
| 1 | 已完成 |
| 2 | 逾期 |

#### 照片公开性 (is_public)
| 值 | 说明 |
|----|------|
| 0 | 私有 |
| 1 | 公开 |

#### 提醒已读状态 (is_read)
| 值 | 说明 |
|----|------|
| 0 | 未读 |
| 1 | 已读 |

---

## 数据库初始化

### MySQL初始化

```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE db_plant_official CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit

# 2. 导入表结构
mysql -u root -p db_plant_official < schema-mysql.sql

# 3. 验证
mysql -u root -p db_plant_official -e "SHOW TABLES;"
```

### SQLite初始化

SQLite数据库会在应用启动时自动初始化，执行 `schema-sqlite.sql` 脚本。

数据库文件位置：`data/user_plant_local.db`

---

## 数据库维护

### 备份

```bash
# MySQL备份
mysqldump -u root -p db_plant_official > backup_$(date +%Y%m%d).sql

# SQLite备份
cp data/user_plant_local.db backup_$(date +%Y%m%d).db
```

### 恢复

```bash
# MySQL恢复
mysql -u root -p db_plant_official < backup_20240101.sql

# SQLite恢复
cp backup_20240101.db data/user_plant_local.db
```

### 性能优化

```sql
-- 分析表
ANALYZE TABLE sys_user;
ANALYZE TABLE care_record;

-- 优化表
OPTIMIZE TABLE sys_user;
OPTIMIZE TABLE care_record;

-- 检查索引使用情况
EXPLAIN SELECT * FROM sys_user WHERE username = 'test';
```

---

## 相关文档

- [快速开始](QUICKSTART.md)
- [开发指南](DEVELOPMENT.md)
- [API文档](API.md)
- [部署指南](DEPLOYMENT.md)
