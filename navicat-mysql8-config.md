# Navicat 连接 MySQL 8 配置指南

## 🎯 快速连接配置

### 基本连接信息
- **连接类型**: MySQL
- **连接名称**: `DZVote MySQL 8`
- **主机名/IP地址**: `localhost`
- **端口**: `3307`
- **用户名**: `dzvote`
- **密码**: `dzvote123`

### 高级配置（推荐）
- **数据库**: `dzvote_v2` (初始数据库)
- **字符集**: `utf8mb4`
- **SSL**: 关闭或使用 Require
- **SSH隧道**: 不需要

## 🔧 连接步骤

### 1. 打开 Navicat
- 点击 `文件` -> `新建连接` -> `MySQL`

### 2. 填写连接信息
```
连接名称: DZVote MySQL 8
主机名或IP地址: localhost
端口: 3307
用户名: dzvote
密码: dzvote123
```

### 3. 高级设置
- 点击 `高级` 选项卡
- 字符集选择: `utf8mb4`
- SSL设置: `不使用SSL` (或根据需求选择)

### 4. 测试连接
- 点击 `测试连接` 按钮
- 应显示 "连接成功"

### 5. 保存并使用
- 点击 `确定` 保存连接
- 双击连接名进行数据库操作

## 📊 可用数据库

### 当前数据库列表
```sql
SHOW DATABASES;
```

- `dzvote_v2` - 主业务数据库
- `mysql` - 系统数据库
- `information_schema` - 信息数据库
- `performance_schema` - 性能数据库

## 🛠️ 常用操作

### 创建新数据库
```sql
CREATE DATABASE `你的数据库名` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 备份数据库
在Navicat中：
1. 右键点击数据库名
2. 选择 `转储SQL文件` -> `结构和数据`

### 导入数据
1. 右键点击数据库名
2. 选择 `运行SQL文件`
3. 选择你的SQL文件

## 🔍 验证连接

执行以下SQL测试：
```sql
-- 查看MySQL版本
SELECT VERSION() as mysql_version;

-- 查看连接数配置
SHOW VARIABLES LIKE 'max_connections';

-- 查看字符集
SHOW VARIABLES LIKE 'character_set%';

-- 查看当前数据库
SELECT DATABASE() as current_database;
```

## 🚨 常见问题

### SSL连接错误
- 解决方案：在高级设置中关闭SSL

### 认证错误
- 确认用户名：`dzvote`
- 确认密码：`dzvote123`
- 确认端口：`3307`

### 权限问题
如需更高权限，可以使用root账号：
- 用户名：`root`
- 密码：`mysql8123`

## 📱 实用提示

1. **性能监控**: 使用Navicat的监控工具查看MySQL性能
2. **备份策略**: 定期备份`dzvote_v2`数据库
3. **字符集**: 确保使用`utf8mb4`支持完整Unicode
4. **索引优化**: 定期检查索引使用情况