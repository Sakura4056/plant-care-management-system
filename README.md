# 植物养护管理系统

## 项目介绍

这是一个基于Java Spring Boot框架开发的植物养护管理系统，旨在帮助植物爱好者更好地管理和养护植物。系统采用前后端分离的架构设计，提供植物信息查询、养护计划制定、养护记录管理、成长相册、智能提醒等功能。

## 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.2 | 核心框架 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| Spring Security | - | 安全框架 |
| JWT | 0.11.5 | 身份认证 |
| MySQL | 8.0+ | 主数据库 |
| SQLite | 3.45.1+ | 本地数据库 |
| Redis | - | 缓存 |
| Maven | - | 项目管理 |

### 前端技术栈

| 技术 | 说明 |
|------|------|
| Vue.js 3.x | 前端框架 |
| Vite | 构建工具 |
| Vue Router | 路由管理 |
| Pinia | 状态管理 |
| Axios | HTTP客户端 |
| Element Plus | UI组件库 |
| ECharts | 图表库 |

## 项目结构

```
Greenly/
├── plant-backend/                  # Spring Boot后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/plant/backend/
│   │   │   │   ├── config/         # 配置类
│   │   │   │   ├── controller/     # 控制器
│   │   │   │   ├── service/        # 业务服务
│   │   │   │   ├── mapper/         # 数据访问
│   │   │   │   ├── entity/         # 实体类
│   │   │   │   ├── dto/            # 数据传输对象
│   │   │   │   ├── exception/      # 异常处理
│   │   │   │   ├── util/           # 工具类
│   │   │   │   ├── task/           # 定时任务
│   │   │   │   └── event/          # 事件处理
│   │   │   └── resources/
│   │   │       ├── db/             # 数据库脚本
│   │   │       └── application.yml # 配置文件
│   │   └── test/                   # 测试代码
│   ├── data/                       # SQLite数据库
│   ├── pom.xml                     # Maven配置
│   └── README.md                   # 后端说明
│
├── plant-frontend/                 # Vue前端项目
│   ├── src/
│   │   ├── api/                    # API接口
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # 公共组件
│   │   ├── layout/                 # 布局组件
│   │   ├── pages/                  # 页面组件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # 状态管理
│   │   ├── App.vue                 # 根组件
│   │   └── main.js                 # 入口文件
│   ├── public/                     # 公共资源
│   ├── package.json                # 依赖配置
│   └── vite.config.js              # Vite配置
│
├── .gitignore                      # Git忽略文件
└── README.md                       # 项目说明
```

## 主要功能模块

### 1. 用户管理模块

- 用户注册与登录
- 个人信息管理
- 角色权限控制（普通用户/管理员）
- JWT Token认证

### 2. 植物信息库模块

- 官方植物信息查询与浏览
- 植物分类管理（按属、种分类）
- 植物详情展示
- 支持关键词搜索

### 3. 我的植物管理模块

- 添加个人养护的植物
- 记录植物基本信息（名称、属、种、描述等）
- 植物审核机制（可提交审核加入官方库）
- 植物状态管理

### 4. 养护计划管理模块

- 创建养护计划
- 设置任务名称和截止时间
- 计划状态跟踪（未完成/已完成/逾期）
- 提醒配置

### 5. 养护记录模块

- 记录浇水、施肥等养护操作
- 记录操作详情（JSON格式存储）
- 养护历史查询
- 数据统计分析
- 图表展示养护趋势

### 6. 成长相册模块

- 上传植物照片
- 支持分片上传大文件
- EXIF信息自动提取
- 照片时间轴展示
- 公开/私有设置

### 7. 提醒通知模块

- 定时扫描到期的养护计划
- 系统内弹窗提醒
- 邮件通知
- 未读提醒统计
- 提醒配置管理

### 8. 管理员功能

- 用户列表管理
- 植物审核工单处理
- 官方植物库维护
- 系统数据统计

## 数据库设计

### 双数据源架构

系统采用MySQL + SQLite双数据源设计：

**MySQL（主数据源）**
- 存储系统核心数据
- 支持多用户并发访问
- 数据表：用户、官方植物、养护计划、养护记录、提醒等

**SQLite（次数据源）**
- 存储用户本地自定义植物数据
- 轻量级嵌入式数据库
- 支持离线操作
- 数据表：本地植物

### 主要数据表

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| official_plant | 官方植物表 |
| local_plant | 本地植物表（SQLite） |
| audit_order | 审核工单表 |
| care_schedule | 养护计划表 |
| care_record | 养护记录表 |
| plant_photo | 成长相册表 |
| reminder | 提醒通知表 |
| reminder_config | 提醒配置表 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis
- Node.js 16+ （前端开发）

### 后端启动

1. 克隆项目
```bash
git clone https://github.com/Sakura4056/plant-care-management-system.git
cd Greenly/plant-backend
```

2. 配置数据库
```bash
# 创建MySQL数据库
mysql -u root -p
CREATE DATABASE db_plant_official CHARACTER SET utf8mb4;

# 导入数据库脚本
mysql -u root -p db_plant_official < src/main/resources/db/schema-mysql.sql
```

3. 修改配置文件 `application.yml`
```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://localhost:3306/db_plant_official
      username: your_username
      password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

4. 启动项目
```bash
mvn spring-boot:run
```

后端服务运行在 http://localhost:8080

### 前端启动

1. 进入前端目录
```bash
cd plant-frontend
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

前端服务运行在 http://localhost:5173

## API接口文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/user/register | 用户注册 |
| POST | /api/user/login | 用户登录 |

### 用户接口

| 方法 | 路径 | 说明 |
|------|------|------|
| PUT | /api/user/update | 更新用户信息 |
| DELETE | /api/user/delete/{userId} | 删除用户（管理员） |
| GET | /api/user/list | 获取用户列表（管理员） |

### 植物接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/plant/official/query | 查询官方植物 |
| GET | /api/plant/local/list | 查询我的植物 |
| POST | /api/plant/local/add | 添加本地植物 |
| POST | /api/plant/audit/submit | 提交审核 |
| POST | /api/plant/audit/{id} | 审核植物（管理员） |

### 养护计划接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/care/schedule/list | 查询养护计划 |
| POST | /api/care/schedule/add | 添加养护计划 |
| PUT | /api/care/schedule/update | 更新养护计划 |
| DELETE | /api/care/schedule/delete/{id} | 删除养护计划 |

### 养护记录接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/care/record/list | 查询养护记录 |
| POST | /api/care/record/add | 添加养护记录 |
| GET | /api/care/record/statistic | 养护数据统计 |

### 照片接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/photo/upload | 上传照片 |
| GET | /api/photo/list | 查询照片列表 |
| PUT | /api/photo/update | 更新照片信息 |
| DELETE | /api/photo/delete/{id} | 删除照片 |

### 提醒接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/reminder/unread | 查询未读提醒 |
| POST | /api/reminder/config | 更新提醒配置 |
| POST | /api/reminder/read/{id} | 标记已读 |

## 部署说明

### 后端部署

1. 打包项目
```bash
mvn clean package -DskipTests
```

2. 运行jar包
```bash
java -jar target/plant-backend-0.0.1-SNAPSHOT.jar
```

### 前端部署

1. 构建生产版本
```bash
npm run build
```

2. 使用Nginx部署
```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 维护与更新

### 数据库备份

```bash
# MySQL备份
mysqldump -u root -p db_plant_official > backup_$(date +%Y%m%d).sql

# SQLite备份
cp data/user_plant_local.db backup_$(date +%Y%m%d).db
```

### 系统更新

```bash
# 拉取最新代码
git pull origin main

# 重启后端服务
# 停止当前服务
# 重新打包并启动
```

### 日志查看

```bash
# 查看应用日志
tail -f logs/plant-backend.log
```

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式

- 项目维护者：Sakura4056
- 邮箱：sakura010903@163.com
- 项目地址：[https://github.com/Sakura4056/plant-care-management-system](https://github.com/Sakura4056/plant-care-management-system)

## 致谢

感谢所有为开源社区做出贡献的开发者们！

---

**注意**：本项目仅供学习交流使用，请勿用于商业用途。
