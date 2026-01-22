# 数据库安装配置指南

## 当前状态
- Activity Service 已运行在 8081 端口
- 需要安装 MySQL 8 和 Redis 来支持完整功能

## 方案1：使用Docker（推荐）

### 启动Docker Desktop
1. 在开始菜单搜索 "Docker Desktop"
2. 启动 Docker Desktop 应用
3. 等待Docker引擎启动（右下角图标变绿）

### 启动数据库服务
```bash
cd d:/ide/toupiao/ROOT_CodeBuddyCN/新架构/docker
docker-compose -f docker-compose-cn.yml up -d mysql redis
```

### 数据库连接信息
- **MySQL**: localhost:3306
  - 用户名: root
  - 密码: root123456
  - 数据库: dzvote_new
- **Redis**: localhost:6379

## 方案2：本地安装

### MySQL 8 安装
1. 下载 MySQL 8.0 Community Server
2. 安装时设置 root 密码为 `123456`
3. 创建数据库：`CREATE DATABASE dzvote_v2;`

### Redis 安装
1. 下载 Redis for Windows
2. 启动 Redis 服务

## 验证数据库连接
```sql
# MySQL连接测试
mysql -h localhost -u root -p
```

```bash
# Redis连接测试
redis-cli ping
```

## 下一步
数据库就绪后，我们将：
1. 部署其他后端服务（vote-service、gateway-service等）
2. 构建和启动前端应用
3. 完成系统整合测试