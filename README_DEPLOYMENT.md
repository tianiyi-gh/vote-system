# DZVOTE 2.0 部署指南

## 📦 项目结构

```
新架构/
├── backend/                    # 后端服务
│   ├── common/                # 公共模块
│   ├── activity-service/      # 活动管理服务
│   ├── vote-service/          # 投票服务
│   ├── user-service/          # 用户服务(TODO)
│   ├── statistics-service/    # 统计服务(TODO)
│   └── payment-service/       # 支付服务(TODO)
├── frontend/                   # 前端项目
│   ├── admin/                 # 管理后台
│   └── h5/                    # 移动端H5(TODO)
├── docker/                     # Docker配置
├── sql/                       # 数据库脚本
└── docs/                      # 文档
```

## 🚀 快速开始

### 方式一：Docker Compose部署（推荐）

#### 1. 准备环境

确保已安装：
- Docker 20.10+
- Docker Compose 2.0+

#### 2. 启动所有服务

```bash
cd 新架构/docker
docker-compose up -d
```

这将自动启动：
- ✅ MySQL 8.0（端口3306）
- ✅ Redis 7.0（端口6379）
- ✅ RabbitMQ 3.12（端口5672/15672）
- ✅ Nacos 2.2.3（端口8848）
- ✅ Activity Service（端口8081）
- ✅ Vote Service（端口8082）
- ✅ Nginx（端口80）

#### 3. 访问服务

- **管理后台**: http://localhost
- **Nacos控制台**: http://localhost:8848/nacos (用户名/密码: nacos/nacos)
- **RabbitMQ管理**: http://localhost:15672 (用户名/密码: admin/admin123)
- **Activity Service API**: http://localhost:8081/swagger-ui.html
- **Vote Service API**: http://localhost:8082/swagger-ui.html

#### 4. 停止服务

```bash
docker-compose down
```

---

### 方式二：本地开发部署

#### 1. 环境准备

**必需软件：**
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+

#### 2. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source 新架构/sql/schema.sql
```

#### 3. 启动后端服务

```bash
# 启动Activity Service
cd 新架构/backend/activity-service
mvn spring-boot:run

# 启动Vote Service（新开终端）
cd 新架构/backend/vote-service
mvn spring-boot:run
```

#### 4. 启动前端

```bash
cd 新架构/frontend/admin
npm install
npm run dev
```

访问：http://localhost:3000

---

## 📊 服务端口说明

| 服务 | 端口 | 用途 |
|------|------|------|
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672 | 消息队列 |
| RabbitMQ管理 | 15672 | 管理界面 |
| Nacos | 8848 | 服务注册/配置中心 |
| Activity Service | 8081 | 活动管理 |
| Vote Service | 8082 | 投票服务 |
| Nginx | 80 | 反向代理 |
| Admin前端 | 3000 | 管理后台(开发) |

---

## 🗄️ 数据库配置

### 生产环境配置

修改 `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-mysql-host:3306/dzvote_v2
    username: your-username
    password: your-password
```

### 数据迁移

从旧系统迁移数据：

```bash
# 使用提供的迁移脚本
cd migration_tools
python migrate_database.py
```

---

## 🔧 配置说明

### Nacos配置

所有服务配置统一管理在Nacos中：

1. 访问 http://localhost:8848/nacos
2. 登录（nacos/nacos）
3. 配置管理 > 配置列表
4. 创建配置：
   - Data ID: `activity-service-dev.yml`
   - Group: `DEFAULT_GROUP`
   - 配置格式: `YAML`

### Redis配置

缓存策略：
- 活动信息：缓存30分钟
- 候选人排名：实时计算，缓存5分钟
- IP限制：按天缓存

### RabbitMQ配置

消息队列用途：
- 投票异步处理
- 票数统计更新
- 排名计算任务

---

## 📈 性能优化

### 数据库优化

```sql
-- 已添加的索引
-- 活动表：service_id, region, status
-- 候选人表：activity_id, service_id, total_votes, score
-- 投票记录表：按年份分区

-- 查看索引使用情况
SHOW INDEX FROM vote_candidate;
```

### Redis缓存

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 1800000  # 30分钟
```

### 连接池配置

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      connection-timeout: 30000
```

---

## 🔒 安全配置

### 1. 修改默认密码

```yaml
# MySQL
MYSQL_ROOT_PASSWORD: 强密码

# Redis
command: redis-server --requirepass 强密码

# Nacos
nacos.core.auth.default.token.secret.key: 自定义密钥
```

### 2. 启用HTTPS

```bash
# 生成SSL证书
cd docker/nginx
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout cert.key -out cert.crt
```

修改 `nginx.conf`:

```nginx
server {
    listen 443 ssl;
    ssl_certificate /etc/nginx/cert.crt;
    ssl_certificate_key /etc/nginx/cert.key;
}
```

---

## 📝 日志管理

### 查看日志

```bash
# Docker环境
docker-compose logs -f activity-service
docker-compose logs -f vote-service

# 本地环境
tail -f logs/activity-service.log
```

### 日志级别配置

```yaml
logging:
  level:
    root: INFO
    com.dzvote: DEBUG
```

---

## 🧪 测试

### 单元测试

```bash
mvn test
```

### 接口测试

访问Swagger文档：
- Activity Service: http://localhost:8081/swagger-ui.html
- Vote Service: http://localhost:8082/swagger-ui.html

---

## 🚨 故障排查

### 常见问题

**1. 服务启动失败**

```bash
# 检查端口占用
netstat -an | findstr "8081"

# 查看日志
docker-compose logs activity-service
```

**2. 数据库连接失败**

```bash
# 测试数据库连接
mysql -h localhost -u root -p
```

**3. Redis连接失败**

```bash
# 测试Redis
redis-cli ping
```

---

## 📊 监控

### Actuator监控

访问：
- http://localhost:8081/actuator/health
- http://localhost:8081/actuator/metrics

### Nacos服务监控

访问 Nacos控制台查看服务健康状态

---

## 🔄 更新部署

### Docker环境

```bash
# 拉取最新代码
git pull

# 重新构建
docker-compose build

# 重启服务
docker-compose restart
```

### 本地环境

```bash
# 后端
mvn clean install
mvn spring-boot:run

# 前端
npm run build
```

---

## 📞 技术支持

遇到问题？
1. 查看日志文件
2. 检查Swagger API文档
3. 查阅源码注释

---

## 🎯 下一步

- [ ] 完成用户服务开发
- [ ] 完成统计服务开发
- [ ] 完成支付服务开发
- [ ] 开发H5投票页面
- [ ] 添加CI/CD流程
- [ ] 性能压测与优化
