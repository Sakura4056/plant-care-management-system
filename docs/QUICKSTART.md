# 快速开始指南

本文档将帮助您快速启动植物养护管理系统。

## 目录

- [环境准备](#环境准备)
- [后端启动](#后端启动)
- [前端启动](#前端启动)
- [常见问题](#常见问题)

## 环境准备

### 必需软件

| 软件 | 版本要求 | 下载地址 |
|------|----------|----------|
| JDK | 17+ | https://www.oracle.com/java/technologies/downloads/ |
| Maven | 3.6+ | https://maven.apache.org/download.cgi |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/mysql/ |
| Redis | 5.0+ | https://redis.io/download/ |
| Node.js | 16+ | https://nodejs.org/ |

### 验证环境

```bash
# 验证Java
java -version

# 验证Maven
mvn -version

# 验证MySQL
mysql --version

# 验证Redis
redis-server --version

# 验证Node.js
node -v
npm -v
```

## 后端启动

### 1. 克隆项目

```bash
git clone https://github.com/Sakura4056/plant-care-management-system.git
cd Greenly
```

### 2. 配置数据库

#### Windows系统

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE db_plant_official CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 退出MySQL
exit

# 导入数据库脚本
mysql -u root -p db_plant_official < plant-backend/src/main/resources/db/schema-mysql.sql
```

#### Linux/Mac系统

```bash
# 创建数据库并导入数据
mysql -u root -p -e "CREATE DATABASE db_plant_official CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p db_plant_official < plant-backend/src/main/resources/db/schema-mysql.sql
```

### 3. 配置Redis

```bash
# 启动Redis服务
# Windows: 运行redis-server.exe
# Linux/Mac: redis-server

# 验证Redis运行
redis-cli ping
# 应返回: PONG
```

### 4. 修改配置文件

编辑 `plant-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://localhost:3306/db_plant_official?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      username: root                    # 修改为你的MySQL用户名
      password: your_password           # 修改为你的MySQL密码
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password     # 如果Redis有密码则配置
      timeout: 10000ms

jwt:
  secret: your-secret-key-32-bytes-minimum  # 修改为自定义密钥
  expiration: 86400000
```

### 5. 启动后端

#### 方式一：使用Maven

```bash
cd plant-backend
mvn spring-boot:run
```

#### 方式二：使用IDEA

1. 用IDEA打开 `plant-backend` 目录
2. 等待Maven依赖下载完成
3. 找到 `PlantBackendApplication.java`
4. 右键运行 `Run 'PlantBackendApplication'`

#### 方式三：打包运行

```bash
cd plant-backend
mvn clean package -DskipTests
java -jar target/plant-backend-0.0.1-SNAPSHOT.jar
```

### 6. 验证后端启动

访问：http://localhost:8080/api/test/hello

应返回：
```json
{
  "code": 200,
  "message": "success",
  "data": "Hello World"
}
```

## 前端启动

### 1. 安装依赖

```bash
cd plant-frontend
npm install
```

如果安装速度慢，可以使用淘宝镜像：

```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### 2. 配置API地址

编辑 `plant-frontend/.env.development`：

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### 3. 启动前端

```bash
npm run dev
```

### 4. 访问系统

- 前端地址：http://localhost:5173
- 后端API：http://localhost:8080/api

## 常见问题

### 1. 端口被占用

```bash
# 查找占用8080端口的进程
netstat -ano | findstr :8080

# 结束进程（替换PID为实际进程ID）
taskkill /F /PID <PID>
```

### 2. 数据库连接失败

- 检查MySQL服务是否启动
- 确认数据库用户名密码配置正确
- 确认数据库已创建：`SHOW DATABASES;`

### 3. Redis连接失败

- 检查Redis服务是否启动
- Windows: 运行 `redis-server.exe`
- Linux/Mac: 运行 `redis-server`

### 4. Maven依赖下载失败

```bash
# 清理缓存后重新下载
cd plant-backend
mvn clean
mvn dependency:resolve
```

或者配置阿里云镜像：

编辑 `~/.m2/settings.xml`（Windows: `C:\Users\你的用户名\.m2\settings.xml`）

```xml
<mirrors>
    <mirror>
        <id>aliyunmaven</id>
        <mirrorOf>*</mirrorOf>
        <name>阿里云公共仓库</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

### 5. 前端npm install失败

```bash
# 清理缓存
npm cache clean --force

# 删除node_modules和package-lock.json
rm -rf node_modules package-lock.json

# 重新安装
npm install
```

### 6. JWT Token无效

- 检查系统时间是否正确
- 确认JWT密钥配置正确
- Token有效期为24小时，超时需要重新登录

## 测试账号

系统启动后可使用以下账号登录测试：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user | user123 |

注意：首次启动需要手动注册账号。

## 下一步

- 阅读 [开发指南](DEVELOPMENT.md) 了解项目结构
- 阅读 [API文档](API.md) 了解接口详情
- 阅读 [部署指南](DEPLOYMENT.md) 学习生产环境部署
