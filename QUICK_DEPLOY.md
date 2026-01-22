# 快速部署指南

## 前提条件

1. **已购买服务器**（推荐配置：2核4G，带宽5M以上）
2. **已备案域名**（用于微信分享功能）
3. **本地开发环境**可以正常编译和运行

## 快速部署步骤

### 1. 服务器初始化（首次部署）

```bash
# 上传服务器初始化脚本
scp server-setup.sh root@your-server:/root/

# 登录服务器
ssh root@your-server

# 执行初始化脚本
cd /root
chmod +x server-setup.sh
./server-setup.sh
```

脚本会自动安装：
- Java 17
- Node.js 18
- MySQL
- Redis
- Nginx
- 配置防火墙
- 创建必要目录

### 2. 创建数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行以下 SQL
CREATE DATABASE dzvote_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'dzvote'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON dzvote_v2.* TO 'dzvote'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3. 修改后端配置

在本地编辑 `application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dzvote_v2?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: dzvote
    password: your_password  # 改为你的密码

  redis:
    host: localhost
    port: 6379
    # password: your_redis_password  # 如果有密码

wechat:
  app-id: wx4a6082a9eb74c84e
  app-secret: 8a2d21736509e9b5eb50d619750e6115
  js-api-domain: tp.dzwww.com

upload:
  path: /opt/vote-service/uploads
```

### 4. 本地编译后端

```bash
cd "D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service"
D:\ide\maven3.9\bin\mvn.cmd clean package -DskipTests
```

### 5. 本地构建前端

```bash
cd 新架构/frontend/h5
npm install
npm run build
```

### 6. 部署到服务器

**Windows 使用 deploy.bat：**

```batch
# 编辑 deploy.bat，设置 SERVER_IP
set SERVER_IP=your-server-ip

# 执行部署
deploy.bat
```

**或手动部署：**

```bash
# 上传后端
scp target/vote-service-2.0.0.jar root@your-server:/opt/vote-service/

# 上传前端
scp -r 新架构/frontend/h5/dist/* root@your-server:/opt/h5/dist/

# 登录服务器
ssh root@your-server
```

### 7. 配置后端服务

```bash
# 上传 systemd 配置
scp vote-service.service root@your-server:/etc/systemd/system/

# 登录服务器
ssh root@your-server

# 启动服务
systemctl daemon-reload
systemctl start vote-service
systemctl enable vote-service

# 查看日志
journalctl -u vote-service -f
```

### 8. 配置 Nginx

```bash
# 上传 Nginx 配置
scp nginx-vote.conf root@your-server:/etc/nginx/conf.d/vote.conf

# 登录服务器
ssh root@your-server

# 修改配置中的域名
nano /etc/nginx/conf.d/vote.conf

# 测试配置
nginx -t

# 重启 Nginx
systemctl restart nginx
```

### 9. 配置微信 JS 接口安全域名

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 进入"设置" → "功能设置"
3. 找到"JS 接口安全域名"
4. 添加：`tp.dzwww.com`
5. 下载验证文件
6. 上传到服务器：`/opt/h5/dist/`

```bash
# 上传验证文件
scp MP_verify_xxx.txt root@your-server:/opt/h5/dist/
```

### 10. 测试

```bash
# 在服务器上检查服务状态
systemctl status vote-service
systemctl status nginx
systemctl status redis
systemctl status mysqld

# 查看端口监听
netstat -tunlp | grep -E "80|8082|3306|6379"
```

在浏览器访问：
- 前端: `http://tp.dzwww.com`
- API: `http://tp.dzwww.com/api`
- Swagger: `http://tp.dzwww.com/swagger-ui.html`

在微信中打开并测试分享功能。

## 常见问题

### Q: 后端启动失败
A: 检查日志 `journalctl -u vote-service -n 100`，常见原因：
- 数据库连接失败：检查 MySQL 密码
- Redis 连接失败：检查 Redis 是否启动
- 端口被占用：`netstat -tunlp | grep 8082`

### Q: 前端无法访问
A: 检查 Nginx 配置和日志：
```bash
nginx -t
tail -f /var/log/nginx/error.log
```

### Q: 微信分享不工作
A: 检查：
- JS 接口安全域名是否配置
- 验证文件是否正确上传
- 微信配置 AppID 和 AppSecret 是否正确

### Q: 如何更新部署
A:
1. 本地重新编译
2. 执行部署脚本 `deploy.bat` 或 `deploy.sh`
3. 自动上传并重启服务

## 备份

### 数据库备份
```bash
# 创建备份目录
mkdir -p /opt/backups

# 手动备份
mysqldump -u dzvote -p dzvote_v2 > /opt/backups/dzvote_v2_$(date +%Y%m%d).sql

# 恢复
mysql -u dzvote -p dzvote_v2 < /opt/backups/dzvote_v2_20260121.sql
```

### 自动备份（定时任务）
```bash
crontab -e

# 添加以下行（每天凌晨 2 点备份）
0 2 * * * /opt/backup.sh
```

## 安全建议

1. **修改默认密码**：数据库、Redis 使用强密码
2. **配置防火墙**：只开放 22, 80, 443 端口
3. **使用 SSH 密钥**：禁用密码登录
4. **启用 HTTPS**：使用 Let's Encrypt 免费证书
5. **定期更新**：系统和依赖包

## 监控命令

```bash
# 服务状态
systemctl status vote-service
systemctl status nginx

# 实时日志
journalctl -u vote-service -f
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log

# 资源使用
top
htop
df -h
free -h
```

## 联系支持

如有问题，请查看详细文档 `DEPLOYMENT_GUIDE.md`
