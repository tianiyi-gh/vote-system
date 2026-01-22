# 投票限制功能升级指南

## 数据库升级（手动执行）

### 方法1：使用 MySQL 客户端

1. 打开 MySQL 客户端（如 Navicat、MySQL Workbench 或命令行）
2. 连接到数据库：
   - 主机: localhost
   - 端口: 3307
   - 用户: root
   - 密码: mysql8123
   - 数据库: dzvote_v2

3. 执行以下 SQL：

```sql
-- 1. 添加每天最多候选人字段
ALTER TABLE activities
ADD COLUMN daily_candidate_limit INT DEFAULT 5 COMMENT '每天最多对多少名候选人投票（例如：5），0表示不限制';

-- 2. 添加候选人每日限制字段
ALTER TABLE activities
ADD COLUMN candidate_daily_limit INT DEFAULT 1 COMMENT '每个候选人每天限投多少次（例如：1），0表示不限制';

-- 3. 更新现有活动的默认值
UPDATE activities SET daily_candidate_limit = 5 WHERE daily_candidate_limit IS NULL;
UPDATE activities SET candidate_daily_limit = 1 WHERE candidate_daily_limit IS NULL;

-- 4. 验证字段是否添加成功
SELECT
    id,
    title,
    vote_limit,
    daily_candidate_limit,
    candidate_daily_limit
FROM activities
LIMIT 5;
```

### 方法2：使用批处理脚本（如果 MySQL 命令行可用）

双击运行：
```
execute_add_vote_limit_fields.bat
```

## 后端升级

### 1. 编译代码

```bash
cd d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service
mvn clean compile -DskipTests
```

### 2. 打包（可选）

```bash
mvn package -DskipTests
```

### 3. 重启服务

```bash
cd d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service
restart.bat
```

或手动重启：
```bash
# 停止现有服务
pkill -f vote-service

# 启动新服务
java -jar target/vote-service-2.0.0.jar
```

## 前端升级

前端无需重新编译，直接刷新后台管理页面即可看到新的设置项。

## 验证升级

### 1. 数据库验证

```sql
SELECT
    id,
    title,
    vote_limit,
    daily_candidate_limit,
    candidate_daily_limit
FROM activities;
```

期望输出：
- vote_limit: 每人总票数限制
- daily_candidate_limit: 每天最多对多少名候选人投票
- candidate_daily_limit: 每个候选人每天限投多少次

### 2. 后台管理验证

1. 登录后台管理系统
2. 进入"活动列表"
3. 编辑某个活动
4. 查看是否新增了以下字段：
   - 每天最多候选人
   - 候选人每日限制

### 3. 投票功能验证

1. 访问投票页面
2. 尝试投票
3. 检查是否正确应用了新的限制规则

## 常见问题

### Q: 如何查看升级是否成功？

A: 执行以下 SQL 检查字段是否存在：
```sql
SHOW COLUMNS FROM activities LIKE 'daily_candidate_limit';
SHOW COLUMNS FROM activities LIKE 'candidate_daily_limit';
```

### Q: 升级失败怎么办？

A: 检查以下几点：
1. MySQL 服务是否正常运行
2. 数据库连接信息是否正确
3. 是否有足够的权限执行 ALTER TABLE
4. activities 表是否存在

### Q: 如何设置不同的限制值？

A: 在后台管理中编辑活动，修改对应的字段值即可：
- 每人投票限制：每人最多可投的总票数
- 每天最多候选人：每天最多可对多少名不同候选人投票
- 候选人每日限制：每个候选人每天最多可投多少次

### Q: 设置为 0 表示什么？

A: 设置为 0 表示不限制该维度。例如：
- vote_limit = 0：不限制每人总票数
- daily_candidate_limit = 0：不限制每天可投的候选人数
- candidate_daily_limit = 0：不限制单个候选人的投票次数

## 回滚方案

如果需要回滚，执行以下 SQL：

```sql
ALTER TABLE activities DROP COLUMN daily_candidate_limit;
ALTER TABLE activities DROP COLUMN candidate_daily_limit;
```

注意：回滚前请备份重要数据！
