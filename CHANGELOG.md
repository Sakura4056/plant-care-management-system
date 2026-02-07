# 更新日志

所有项目重要变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)

## [1.0.0] - 2024-01-15

### 新增

- ✅ 完成用户管理模块（注册、登录、信息更新）
- ✅ 完成植物信息库模块（官方植物、本地植物）
- ✅ 完成养护计划管理模块
- ✅ 完成养护记录模块
- ✅ 完成成长相册模块（支持分片上传）
- ✅ 完成提醒通知模块（定时任务、邮件通知）
- ✅ 完成管理员功能（用户管理、植物审核）
- ✅ 实现MySQL + SQLite双数据源架构
- ✅ 实现JWT身份认证
- ✅ 实现Spring Security权限控制
- ✅ 添加完整的API文档
- ✅ 添加快速开始指南
- ✅ 添加开发指南
- ✅ 添加部署指南
- ✅ 添加数据库设计文档

### 技术栈

#### 后端
- Java 17
- Spring Boot 3.2.2
- MyBatis Plus 3.5.5
- Spring Security
- JWT 0.11.5
- MySQL 8.0+
- SQLite 3.45.1+
- Redis

#### 前端
- Vue.js 3.x
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios
- ECharts

### 数据库

#### MySQL表
- sys_user（用户表）
- official_plant（官方植物表）
- audit_order（审核工单表）
- care_schedule（养护计划表）
- care_record（养护记录表）
- plant_photo（植物照片表）
- reminder（提醒通知表）
- reminder_config（提醒配置表）

#### SQLite表
- local_plant（本地植物表）

### 已知问题

- 暂无

---

## 版本说明

版本号格式：`主版本号.次版本号.修订号`

- 主版本号：不兼容的API修改
- 次版本号：向下兼容的功能性新增
- 修订号：向下兼容的问题修正

---

[1.0.0]: https://github.com/Sakura4056/plant-care-management-system/releases/tag/v1.0.0
