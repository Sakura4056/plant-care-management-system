# 部署指南

本文档介绍如何在生产环境部署植物养护管理系统。

## 目录

- [环境要求](#环境要求)
- [后端部署](#后端部署)
- [前端部署](#前端部署)
- [Docker部署](#docker部署)
- [Nginx配置](#nginx配置)
- [数据库备份](#数据库备份)
- [监控与日志](#监控与日志)

## 环境要求

### 服务器配置

| 配置项 | 最低要求 | 推荐配置 |
|--------|----------|----------|
| CPU | 2核 | 4核+ |
| 内存 | 4GB | 8GB+ |
| 硬盘 | 20GB | 50GB+ SSD |
| 带宽 | 1Mbps | 5Mbps+ |

### 软件环境

| 软件 | 版本要求 |
|------|----------|
| 操作系统 | CentOS 7+ / Ubuntu 18+ / Windows Server 2016+ |
| JDK | 17+ |
| MySQL | 8.0+ |
| Redis | 5.0+ |
| Nginx | 1.18+ |

## 后端部署

### 1. 准备环境

```bash
# 安装JDK 17
# CentOS/RHEL
sudo yum install java-17-openjdk java-17-openjdk-devel

# Ubuntu/Debian
sudo apt install openjdk-17-jdk openjdk-17-jre

# 验证
java -version

# 安装MySQL
# CentOS/RHEL
sudo yum install mysql-server

# Ubuntu/Debian
sudo apt install mysql-server

# 安装Redis
# CentOS/RHEL
sudo yum install redis

# Ubuntu/Debian
sudo apt install redis-server
```

### 2. 配置数据库

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE db_plant_official CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建专用用户（推荐）
CREATE USER 'plant_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON db_plant_official.* TO 'plant_user'@'localhost';
FLUSH PRIVILEGES;

# 退出并导入数据
mysql -u plant_user -p db_plant_official < schema-mysql.sql
```

### 3. 配置Redis

```bash
# 编辑Redis配置
sudo vi /etc/redis.conf

# 设置密码（推荐）
requirepass your_redis_password

# 重启Redis
sudo systemctl restart redis

# 验证
redis-cli -a your_redis_password ping
```

### 4. 编译打包

```bash
# 克隆项目
git clone https://github.com/Sakura4056/plant-care-management-system.git
cd Greenly/plant-backend

# 修改配置文件
vi src/main/resources/application.yml

# 打包（跳过测试）
mvn clean package -DskipTests

# 上传到服务器
scp target/plant-backend-0.0.1-SNAPSHOT.jar user@server:/opt/plant/
```

### 5. 创建启动脚本

创建 `/opt/plant/start.sh`：

```bash
#!/bin/bash

APP_NAME=plant-backend
JAR_FILE=/opt/plant/plant-backend-0.0.1-SNAPSHOT.jar
LOG_DIR=/opt/plant/logs
PID_FILE=/opt/plant/app.pid

# 创建日志目录
mkdir -p $LOG_DIR

# 启动应用
nohup java -jar \
  -Xms512m \
  -Xmx1024m \
  -XX:+UseG1GC \
  -Dspring.profiles.active=prod \
  $JAR_FILE > $LOG_DIR/console.log 2>&1 &

echo $! > $PID_FILE
echo "$APP_NAME started with PID: $(cat $PID_FILE)"
```

创建 `/opt/plant/stop.sh`：

```bash
#!/bin/bash

PID_FILE=/opt/plant/app.pid

if [ -f $PID_FILE ]; then
    PID=$(cat $PID_FILE)
    kill $PID
    rm $PID_FILE
    echo "Application stopped"
else
    echo "PID file not found"
fi
```

创建 `/opt/plant/restart.sh`：

```bash
#!/bin/bash

/opt/plant/stop.sh
sleep 5
/opt/plant/start.sh
```

添加执行权限：

```bash
chmod +x /opt/plant/*.sh
```

### 6. 配置系统服务（推荐）

创建 `/etc/systemd/system/plant-backend.service`：

```ini
[Unit]
Description=Plant Backend Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=plant
WorkingDirectory=/opt/plant
ExecStart=/usr/bin/java -jar \
  -Xms512m \
  -Xmx1024m \
  -XX:+UseG1GC \
  -Dspring.profiles.active=prod \
  /opt/plant/plant-backend-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable plant-backend
sudo systemctl start plant-backend
sudo systemctl status plant-backend
```

## 前端部署

### 1. 构建项目

```bash
cd plant-frontend

# 安装依赖
npm install

# 构建生产版本
npm run build

# 上传到服务器
scp -r dist/* user@server:/var/www/plant/
```

### 2. 配置Nginx

见下文[Nginx配置](#nginx配置)

## Docker部署

### 1. 创建Dockerfile

创建 `plant-backend/Dockerfile`：

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/plant-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Xms512m", "-Xmx1024m", "app.jar"]
```

### 2. 创建docker-compose.yml

创建根目录 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: plant-mysql
    environment:
      MYSQL_ROOT_PASSWORD: your_root_password
      MYSQL_DATABASE: db_plant_official
      MYSQL_USER: plant_user
      MYSQL_PASSWORD: your_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./plant-backend/src/main/resources/db/schema-mysql.sql:/docker-entrypoint-initdb.d/schema.sql
    networks:
      - plant-network

  redis:
    image: redis:7-alpine
    container_name: plant-redis
    command: redis-server --requirepass your_redis_password
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - plant-network

  backend:
    build: ./plant-backend
    container_name: plant-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_PRIMARY_JDBC_URL: jdbc:mysql://mysql:3306/db_plant_official
      SPRING_DATASOURCE_PRIMARY_USERNAME: plant_user
      SPRING_DATASOURCE_PRIMARY_PASSWORD: your_password
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PASSWORD: your_redis_password
    depends_on:
      - mysql
      - redis
    networks:
      - plant-network
    restart: always

  frontend:
    image: nginx:alpine
    container_name: plant-frontend
    ports:
      - "80:80"
    volumes:
      - ./plant-frontend/dist:/usr/share/nginx/html
      - ./nginx/nginx.conf:/etc/nginx/conf.d/default.conf
    depends_on:
      - backend
    networks:
      - plant-network

volumes:
  mysql_data:
  redis_data:

networks:
  plant-network:
    driver: bridge
```

### 3. 启动容器

```bash
# 构建并启动
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## Nginx配置

### 基础配置

创建 `/etc/nginx/conf.d/plant.conf`：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端
    location / {
        root /var/www/plant;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 文件上传
    location /uploads {
        alias /opt/plant/uploads;
        expires 30d;
    }

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
```

### HTTPS配置（推荐）

```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/nginx/ssl/your-domain.crt;
    ssl_certificate_key /etc/nginx/ssl/your-domain.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 前端
    location / {
        root /var/www/plant;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 重载配置

```bash
# 测试配置
sudo nginx -t

# 重载配置
sudo nginx -s reload
```

## 数据库备份

### 创建备份脚本

创建 `/opt/plant/backup.sh`：

```bash
#!/bin/bash

BACKUP_DIR=/opt/plant/backups
DATE=$(date +%Y%m%d_%H%M%S)
MYSQL_USER=plant_user
MYSQL_PASSWORD=your_password
MYSQL_DB=db_plant_official

# 创建备份目录
mkdir -p $BACKUP_DIR

# MySQL备份
mysqldump -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DB \
  --single-transaction \
  --quick \
  --lock-tables=false \
  > $BACKUP_DIR/mysql_$DATE.sql

# SQLite备份
cp /opt/plant/data/user_plant_local.db $BACKUP_DIR/sqlite_$DATE.db

# 压缩备份
gzip $BACKUP_DIR/mysql_$DATE.sql

# 删除30天前的备份
find $BACKUP_DIR -type f -mtime +30 -delete

echo "Backup completed: $DATE"
```

### 设置定时任务

```bash
# 编辑crontab
crontab -e

# 添加每天凌晨2点备份
0 2 * * * /opt/plant/backup.sh >> /opt/plant/logs/backup.log 2>&1
```

## 监控与日志

### 查看应用日志

```bash
# 查看控制台日志
tail -f /opt/plant/logs/console.log

# 查看系统日志（systemd）
journalctl -u plant-backend -f

# 查看Nginx访问日志
tail -f /var/log/nginx/access.log

# 查看Nginx错误日志
tail -f /var/log/nginx/error.log
```

### 日志轮转配置

创建 `/etc/logrotate.d/plant-backend`：

```
/opt/plant/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    notifempty
    create 0644 plant plant
    sharedscripts
}
```

### 性能监控

```bash
# 安装监控工具
sudo yum install htop iotop iftop

# CPU/内存监控
htop

# 磁盘IO监控
iotop

# 网络监控
iftop
```

## 更新部署

### 后端更新

```bash
# 1. 本地打包
mvn clean package -DskipTests

# 2. 上传
scp target/plant-backend-0.0.1-SNAPSHOT.jar user@server:/opt/plant/

# 3. 重启
sudo systemctl restart plant-backend
```

### 前端更新

```bash
# 1. 本地构建
npm run build

# 2. 上传
scp -r dist/* user@server:/var/www/plant/

# 3. 重载Nginx
sudo nginx -s reload
```

## 故障排查

### 应用无法启动

```bash
# 检查端口占用
netstat -tlnp | grep 8080

# 检查Java进程
ps aux | grep java

# 查看启动日志
tail -100 /opt/plant/logs/console.log
```

### 数据库连接失败

```bash
# 测试数据库连接
mysql -u plant_user -p -h localhost db_plant_official

# 检查MySQL状态
sudo systemctl status mysql

# 查看MySQL错误日志
tail -f /var/log/mysqld.log
```

### Redis连接失败

```bash
# 测试Redis连接
redis-cli -a your_redis_password ping

# 检查Redis状态
sudo systemctl status redis

# 查看Redis日志
tail -f /var/log/redis/redis.log
```

## 相关文档

- [快速开始](QUICKSTART.md)
- [开发指南](DEVELOPMENT.md)
- [API文档](API.md)
