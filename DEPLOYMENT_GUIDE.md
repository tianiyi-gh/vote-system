# 投票系统部署指南

## 部署架构

```
服务器
├── nginx (端口 80/443) - 反向代理
│   ├── /api -> 后端 vote-service (端口 8082)
│   └── / -> 前端 h5 (端口 3000)
├── vote-service (后端 Spring Boot)
│   ├── 端口: 8082
│   ├── 数据库: MySQL (端口 3307)
│   └── 缓存: Redis (端口 6379)
├── h5 (前端 Vue + Vant)
│   ├── 端口: 3000
│   └── 静态资源
└── 上传文件目录
    └── uploads/
```

## 服务器要求

- **操作系统**: Linux (推荐 CentOS 7+ 或 Ubuntu 18.04+)
- **Java**: JDK 17+
- **Node.js**: 18+ (用于前端构建)
- **Nginx**: 1.18+
- **MySQL**: 5.7+ 或 8.0+
- **Redis**: 6.0+

## 部署步骤

### 1. 准备服务器环境

#### 1.1 安装 JDK 17
```bash
# 检查是否已安装
java -version

# CentOS
sudo yum install java-17-openjdk-devel

# Ubuntu
sudo apt update
sudo apt install openjdk-17-jdk

# 设置环境变量
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

#### 1.2 安装 Node.js
```bash
# 使用 nvm 安装
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 18
nvm use 18
```

#### 1.3 安装 MySQL
```bash
# CentOS
sudo yum install mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld

# Ubuntu
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql

# 创建数据库
mysql -u root -p
```

```sql
CREATE DATABASE dzvote_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'dzvote'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON dzvote_v2.* TO 'dzvote'@'localhost';
FLUSH PRIVILEGES;
```

#### 1.4 安装 Redis
```bash
# CentOS
sudo yum install redis
sudo systemctl start redis
sudo systemctl enable redis

# Ubuntu
sudo apt install redis-server
sudo systemctl start redis
sudo systemctl enable redis

# 设置密码（可选）
sudo nano /etc/redis/redis.conf
# 找到 requirepass 行，设置密码
sudo systemctl restart redis
```

#### 1.5 安装 Nginx
```bash
# CentOS
sudo yum install nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# Ubuntu
sudo apt install nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 2. 部署后端

#### 2.1 上传代码
```bash
# 在服务器上创建目录
mkdir -p /opt/vote-service
cd /opt/vote-service

# 上传后端代码到服务器
# 使用 scp 或 git
```

#### 2.2 修改配置
```bash
cd /opt/vote-service/src/main/resources
nano application.yml
```

修改以下配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dzvote_v2?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: dzvote
    password: your_password

  redis:
    host: localhost
    port: 6379
    password: your_redis_password  # 如果设置了密码

wechat:
  app-id: wx4a6082a9eb74c84e
  app-secret: 8a2d21736509e9b5eb50d619750e6115
  js-api-domain: tp.dzwww.com

upload:
  path: /opt/vote-service/uploads
```

#### 2.3 创建上传目录
```bash
mkdir -p /opt/vote-service/uploads
chmod 755 /opt/vote-service/uploads
```

#### 2.4 打包后端
```bash
# 在本地开发环境
cd "D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service"
D:\ide\maven3.9\bin\mvn.cmd clean package -DskipTests

# 生成的 jar 文件在 target/vote-service-2.0.0.jar
```

#### 2.5 上传 jar 文件
```bash
# 在本地 Windows 使用 scp
scp target/vote-service-2.0.0.jar user@your-server:/opt/vote-service/
```

#### 2.6 创建 systemd 服务
```bash
sudo nano /etc/systemd/system/vote-service.service
```

```ini
[Unit]
Description=Vote Service
After=network.target

[Service]
Type=simple
User=your_username
WorkingDirectory=/opt/vote-service
ExecStart=/usr/bin/java -jar /opt/vote-service/vote-service-2.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 启动服务
sudo systemctl daemon-reload
sudo systemctl start vote-service
sudo systemctl enable vote-service
sudo systemctl status vote-service

# 查看日志
sudo journalctl -u vote-service -f
```

### 3. 部署前端

#### 3.1 上传代码
```bash
# 在服务器上创建目录
mkdir -p /opt/h5
cd /opt/h5

# 上传前端代码
# 使用 scp 或 git
```

#### 3.2 修改 API 地址
```bash
cd /opt/h5/src
nano api.ts
```

修改 API 基础地址：
```typescript
const baseURL = 'http://tp.dzwww.com/api'  // 修改为你的域名
```

#### 3.3 安装依赖并构建
```bash
cd /opt/h5
npm install
npm run build

# 构建产物在 dist 目录
```

#### 3.4 使用 Nginx 托管前端
```bash
sudo nano /etc/nginx/conf.d/h5.conf
```

```nginx
server {
    listen 80;
    server_name tp.dzwww.com;

    # 前端静态文件
    location / {
        root /opt/h5/dist;
        try_files $uri $uri/ /index.html;
        index index.html;

        # 启用 gzip 压缩
        gzip on;
        gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://localhost:8082;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 上传文件目录
    location /uploads {
        alias /opt/vote-service/uploads;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

#### 3.5 测试并重启 Nginx
```bash
# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
sudo systemctl enable nginx
```

### 4. 配置微信 JS 接口安全域名

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 进入"设置" → "功能设置"
3. 找到"JS 接口安全域名"
4. 添加：`tp.dzwww.com`
5. 上传验证文件

下载验证文件：
```bash
cd /opt/h5/dist
# 上传微信提供的验证文件到这个目录
```

### 5. 配置防火墙

```bash
# CentOS (firewalld)
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --reload

# Ubuntu (ufw)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp
sudo ufw enable
```

### 6. 配置 SSL 证书（推荐）

使用 Let's Encrypt 免费证书：

```bash
# 安装 certbot
sudo yum install certbot python2-certbot-nginx  # CentOS
# 或
sudo apt install certbot python3-certbot-nginx  # Ubuntu

# 获取证书
sudo certbot --nginx -d tp.dzwww.com

# 自动续期
sudo certbot renew --dry-run
```

### 7. 开机自启动

```bash
# 后端
sudo systemctl enable vote-service

# Nginx
sudo systemctl enable nginx

# Redis
sudo systemctl enable redis

# MySQL
sudo systemctl enable mysqld
```

## 部署脚本

可以使用以下脚本自动化部署：

### 部署脚本 (deploy.sh)
```bash
#!/bin/bash

# 配置变量
SERVER_USER="user"
SERVER_IP="your-server-ip"
APP_DIR="/opt/vote-service"
FRONTEND_DIR="/opt/h5"
JAR_FILE="vote-service-2.0.0.jar"

echo "开始部署..."

# 1. 上传后端
echo "上传后端..."
scp target/$JAR_FILE $SERVER_USER@$SERVER_IP:$APP_DIR/

# 2. 重启后端
echo "重启后端服务..."
ssh $SERVER_USER@$SERVER_IP "sudo systemctl restart vote-service"

# 3. 构建前端
echo "构建前端..."
cd h5
npm install
npm run build

# 4. 上传前端
echo "上传前端..."
scp -r dist/* $SERVER_USER@$SERVER_IP:$FRONTEND_DIR/dist/

# 5. 重启 Nginx
echo "重启 Nginx..."
ssh $SERVER_USER@$SERVER_IP "sudo systemctl restart nginx"

echo "部署完成！"
```

使用方法：
```bash
# 在本地执行
chmod +x deploy.sh
./deploy.sh
```

## 监控和日志

### 查看后端日志
```bash
sudo journalctl -u vote-service -f
```

### 查看 Nginx 日志
```bash
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### 查看 Redis 日志
```bash
sudo tail -f /var/log/redis/redis-server.log
```

### 查看 MySQL 日志
```bash
sudo tail -f /var/log/mysqld.log
```

## 备份

### 数据库备份
```bash
# 创建备份脚本
nano /opt/backup.sh
```

```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/backups"
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u dzvote -p dzvote_v2 > $BACKUP_DIR/dzvote_v2_$DATE.sql

# 保留最近 7 天的备份
find $BACKUP_DIR -name "dzvote_v2_*.sql" -mtime +7 -delete

echo "备份完成: $BACKUP_DIR/dzvote_v2_$DATE.sql"
```

```bash
# 添加定时任务
chmod +x /opt/backup.sh
crontab -e

# 每天凌晨 2 点备份
0 2 * * * /opt/backup.sh
```

## 故障排查

### 后端无法启动
```bash
# 检查端口占用
netstat -tunlp | grep 8082

# 查看详细日志
sudo journalctl -u vote-service -n 100 --no-pager
```

### 前端无法访问
```bash
# 检查 Nginx 配置
sudo nginx -t

# 查看错误日志
sudo tail -f /var/log/nginx/error.log
```

### 数据库连接失败
```bash
# 检查 MySQL 是否运行
sudo systemctl status mysqld

# 测试连接
mysql -u dzvote -p -h localhost dzvote_v2
```

## 安全建议

1. **修改默认密码**：数据库、Redis 密码使用强密码
2. **配置防火墙**：只开放必要的端口
3. **使用 HTTPS**：配置 SSL 证书
4. **定期备份**：自动备份数据库
5. **更新系统**：定期更新系统和依赖包
6. **限制 SSH 访问**：使用密钥认证，禁用密码登录
