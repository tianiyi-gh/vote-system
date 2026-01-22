# DZVOTE投票系统 - 全新架构

## 项目结构

```
新架构/
├── backend/                    # Spring Boot后端
│   ├── gateway/               # 网关服务
│   ├── activity-service/      # 活动管理服务
│   ├── vote-service/          # 投票服务
│   ├── user-service/          # 用户管理服务
│   ├── statistics-service/    # 统计分析服务
│   ├── payment-service/       # 支付服务
│   └── common/                # 公共模块
├── frontend/                   # 前端项目
│   ├── admin/                 # Vue3管理后台
│   └── h5/                    # Vue3移动端
├── docker/                     # Docker配置
└── docs/                      # 文档
```

## 技术栈

### 后端
- **Spring Boot 3.1.0**
- **Spring Cloud 2022.0.3**
- **MyBatis Plus 3.5.3**
- **Redis 7.0**
- **RabbitMQ 3.12**
- **MySQL 8.0**

### 前端
- **Vue 3.3**
- **Vite 4.3**
- **TypeScript 5.0**
- **Element Plus**
- **Pinia**

## 快速开始

参见各子项目的README文档
