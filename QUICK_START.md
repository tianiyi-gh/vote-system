# 🚀 DZVOTE 2.0 快速开始

## ⚡ 5分钟体验新系统

### 第一步：初始化数据库

```bash
# 1. 创建数据库
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS dzvote_v2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 导入表结构
mysql -u root -p123456 dzvote_v2 < 新架构/sql/schema.sql

# 3. 验证
mysql -u root -p123456 dzvote_v2 -e "SHOW TABLES;"
```

**预期输出**：
```
+-------------------------+
| Tables_in_dzvote_v2     |
+-------------------------+
| sys_user                |
| vote_activity           |
| vote_candidate          |
| vote_limit              |
| vote_record             |
| vote_statistics         |
+-------------------------+
```

---

### 第二步：启动后端服务

#### 方式A：使用IDE（推荐新手）

**IDEA/Eclipse用户：**
1. 导入Maven项目：`新架构/backend/pom.xml`
2. 等待依赖下载完成
3. 运行 `ActivityServiceApplication.java`
4. 运行 `VoteServiceApplication.java`

**看到以下输出表示成功：**
```
========================================
活动管理服务启动成功！
Swagger文档地址: http://localhost:8081/swagger-ui.html
========================================
```

#### 方式B：使用命令行

```bash
# 终端1：启动活动服务
cd 新架构/backend/activity-service
mvn spring-boot:run

# 终端2：启动投票服务
cd 新架构/backend/vote-service
mvn spring-boot:run
```

---

### 第三步：启动前端

```bash
cd 新架构/frontend/admin

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

**浏览器自动打开**: http://localhost:3000

---

### 第四步：体验系统

#### 1. 测试API（可选）

访问Swagger文档：http://localhost:8081/swagger-ui.html

**创建一个测试活动：**

```bash
curl -X POST "http://localhost:8081/api/activities" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试活动",
    "region": "山东",
    "startTime": "2025-01-01T00:00:00",
    "endTime": "2025-12-31T23:59:59",
    "status": 1
  }'
```

#### 2. 查看管理后台

访问：http://localhost:3000

你会看到：
- ✅ 漂亮的仪表盘
- ✅ 活动列表
- ✅ 数据统计

---

## 🎯 下一步做什么？

### 开发新功能

1. **添加候选人管理**
   - 文件：`backend/activity-service/src/main/java/com/dzvote/activity/service/CandidateService.java`
   - 前端：`frontend/admin/src/views/candidate/list.vue`

2. **实现投票功能**
   - 文件：`backend/vote-service/src/main/java/com/dzvote/vote/service/VoteService.java`

3. **开发H5投票页面**
   - 目录：`frontend/h5/`

### 数据迁移

从旧系统导入数据：

```bash
cd migration_tools
python migrate_database.py
```

### 部署到生产环境

```bash
cd 新架构/docker
docker-compose up -d
```

---

## ❓ 常见问题

### Q1: Maven下载依赖很慢？

**A:** 配置国内镜像 `~/.m2/settings.xml`:

```xml
<mirror>
  <id>aliyun</id>
  <mirrorOf>central</mirrorOf>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

### Q2: npm install失败？

**A:** 使用淘宝镜像:

```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### Q3: 数据库连接失败？

**A:** 检查配置文件 `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dzvote_v2
    username: root
    password: 123456  # 确认密码正确
```

### Q4: 端口被占用？

**A:** 修改端口:

```yaml
# activity-service/src/main/resources/application.yml
server:
  port: 8081  # 改为其他端口
```

---

## 📁 项目结构说明

```
新架构/
├── backend/                    # 后端Spring Boot
│   ├── common/                # 公共模块（Result、异常等）
│   ├── activity-service/      # 活动管理（已完成✅）
│   │   ├── entity/           # 实体类
│   │   ├── mapper/           # 数据访问层
│   │   ├── service/          # 业务逻辑层
│   │   └── controller/       # 控制器
│   └── vote-service/          # 投票服务（框架✅）
│
├── frontend/                   # 前端Vue3
│   └── admin/                 # 管理后台（已完成✅）
│       ├── src/
│       │   ├── views/        # 页面
│       │   ├── router/       # 路由
│       │   └── layout/       # 布局
│       └── package.json
│
├── sql/                        # 数据库脚本
│   └── schema.sql             # 表结构（已优化✅）
│
└── docker/                     # Docker部署
    ├── docker-compose.yml     # 一键部署（已配置✅）
    └── nginx/                 # Nginx配置
```

---

## 🎉 恭喜！

你已经成功启动了DZVOTE 2.0系统！

**当前进度**：
- ✅ 数据库设计完成
- ✅ 后端框架搭建完成
- ✅ 管理后台开发完成
- ✅ Docker部署配置完成

**接下来**：
- ⏳ 完善投票业务逻辑
- ⏳ 开发H5投票页面
- ⏳ 数据统计分析
- ⏳ 支付集成

---

## 📚 文档索引

- [完整部署指南](./README_DEPLOYMENT.md)
- [API文档](http://localhost:8081/swagger-ui.html)
- [数据库设计](./sql/schema.sql)
- [Docker部署](./docker/docker-compose.yml)

---

**遇到问题？** 查看日志：

```bash
# 后端日志
tail -f backend/activity-service/logs/application.log

# 前端控制台
浏览器按F12查看Console
```
