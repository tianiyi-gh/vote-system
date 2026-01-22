# 投票系统修复指南

## 问题总结

1. **前台投票限制不起作用** - 可以一直投票
2. **后台封面图上传失败**
3. **后台编辑页字段不匹配** - 每天最多候选人和候选人每日限制没有与数据库字段匹配

---

## 修复步骤

### 1. 数据库升级（必须执行）

首先检查数据库中是否已有新字段：

```bash
# 方法1：使用 MySQL 客户端
mysql -h localhost -P 3307 -u root -pmysql8123 dzvote_v2 < check_db_fields.sql
```

如果字段不存在，执行以下 SQL：

```sql
-- 1. 添加每天最多候选人字段
ALTER TABLE activities
ADD COLUMN daily_candidate_limit INT DEFAULT 5
COMMENT '每天最多对多少名候选人投票（例如：5），0表示不限制';

-- 2. 添加候选人每日限制字段
ALTER TABLE activities
ADD COLUMN candidate_daily_limit INT DEFAULT 1
COMMENT '每个候选人每天限投多少次（例如：1），0表示不限制';

-- 3. 更新现有活动的默认值
UPDATE activities SET daily_candidate_limit = 5 WHERE daily_candidate_limit IS NULL;
UPDATE activities SET candidate_daily_limit = 1 WHERE candidate_daily_limit IS NULL;

-- 4. 验证
SELECT
    id,
    title,
    vote_limit,
    daily_candidate_limit,
    candidate_daily_limit
FROM activities
LIMIT 5;
```

### 2. 后端编译和重启

```bash
cd d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service

# 编译
mvn clean compile -DskipTests

# 重启服务
restart.bat
```

### 3. 创建上传目录

```bash
cd d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service

# 运行 Python 脚本创建目录
python fix_upload_path.py
```

或手动创建：
```bash
mkdir -p uploads/avatars/2025/01/15
```

### 4. 前端无需修改

前端已自动刷新，无需重新编译。

---

## 问题分析和修复详情

### 问题1：前台投票限制不起作用

**原因：**
- 前端投票请求中没有传递 `voterId` 或 `voterPhone`
- `checkPersonVoteLimit` 方法中 `voterId` 为 null 时直接返回 true（不限制）
- 基于用户标识的限制检查无法生效

**修复：**
已在 `Vote.vue` 中添加用户指纹生成逻辑：
```javascript
// 生成用户唯一标识（基于浏览器指纹 + 时间戳）
const userFingerprint = localStorage.getItem('vote_user_id') ||
                      'user_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
localStorage.setItem('vote_user_id', userFingerprint)

// 在投票请求中包含用户标识
const voteRequest = {
  activityId: activity.value.id,
  candidateId: selectedCandidate.value.id,
  channel: 'WEB',
  captcha: 'SLIDER_PASSED',
  captchaKey: 'slider_' + Date.now(),
  voterId: userFingerprint,    // ← 新增
  voterPhone: userFingerprint    // ← 新增
}
```

### 问题2：后台封面图上传失败

**原因：**
- 上传目录可能不存在
- 缺少详细的错误日志

**修复：**
1. 在 `FileUploadController.java` 中添加了详细的日志
2. 创建了 `fix_upload_path.py` 脚本来自动创建必要的目录
3. 添加了异常堆栈打印

**验证上传：**
上传成功后，后端日志应显示：
```
========== 开始上传头像 ==========
文件名: xxx.jpg
文件大小: xxx bytes
文件类型: image/jpeg
基础上传路径: D:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\uploads
...
========== 上传完成 ==========
```

### 问题3：后台编辑页字段不匹配

**原因：**
- 数据库中可能还没有新字段
- 前端代码已经更新，但数据库字段不存在导致数据无法保存

**修复：**
1. 执行数据库升级脚本（见上方步骤1）
2. 前端代码已包含新字段的映射：
   - `dailyCandidateLimit` → `daily_candidate_limit`
   - `candidateDailyLimit` → `candidate_daily_limit`

**验证：**
登录后台管理系统，编辑活动，查看是否显示：
- ✓ 每人投票限制
- ✓ 每天最多候选人
- ✓ 候选人每日限制

---

## 验证步骤

### 1. 验证投票限制

访问投票页面：http://10.19.95.128:3001/vote/ACT000006

**测试场景1：每人总票数限制**
- 假设活动配置：`vote_limit = 3`
- 连续投票 3 次 → ✓ 成功
- 投第 4 次 → ✗ 失败，提示"您已达到该活动的投票限制"

**测试场景2：每天最多候选人数量限制**
- 假设活动配置：`daily_candidate_limit = 2`
- 对候选人 A 投票 → ✓ 成功
- 对候选人 B 投票 → ✓ 成功
- 对候选人 C 投票 → ✗ 失败，提示"您今天已达到最多可投候选人数限制"

**测试场景3：单个候选人每日限制**
- 假设活动配置：`candidate_daily_limit = 1`
- 对候选人 A 投票 → ✓ 成功
- 再次对候选人 A 投票 → ✗ 失败，提示"您今天对该候选人的投票次数已达到限制"

### 2. 验证文件上传

1. 登录后台管理系统
2. 进入"活动列表"
3. 编辑某个活动
4. 点击"封面图"上传区域
5. 选择一个图片文件
6. 查看是否上传成功并显示预览

**验证日志：**
后端日志应显示完整的上传流程和成功消息。

### 3. 验证后台字段

1. 登录后台管理系统
2. 编辑某个活动
3. 检查以下字段是否显示并可以编辑：
   - 每人投票限制（数字输入框）
   - 每天最多候选人（数字输入框）
   - 候选人每日限制（数字输入框）
4. 修改这些字段的值
5. 点击"保存"
6. 重新编辑该活动，确认修改已保存

---

## 常见问题排查

### Q1: 数据库升级失败

**可能原因：**
- MySQL 服务未启动
- 连接信息不正确
- 权限不足

**排查方法：**
```bash
# 检查 MySQL 服务状态
mysql -h localhost -P 3307 -u root -pmysql8123 -e "SELECT 1"

# 检查字段是否存在
mysql -h localhost -P 3307 -u root -pmysql8123 dzvote_v2 -e "SHOW COLUMNS FROM activities LIKE 'daily_candidate_limit';"
```

### Q2: 投票限制仍然不起作用

**排查方法：**

1. **检查前端日志**
   - 打开浏览器开发者工具（F12）
   - 查看 Console 标签页
   - 查看投票请求的 payload
   - 确认包含 `voterId` 和 `voterPhone`

2. **检查后端日志**
   - 查看后端日志文件
   - 搜索关键词："投票成功"、"投票失败"、"限制"
   - 确认限制检查方法被调用

3. **验证数据库字段**
   ```sql
   SELECT vote_limit, daily_candidate_limit, candidate_daily_limit
   FROM activities
   WHERE id = <活动ID>;
   ```

4. **检查 Redis 缓存**
   - 如果使用了 Redis，检查缓存键是否存在
   - 可能需要手动清除缓存

### Q3: 文件上传仍然失败

**排查方法：**

1. **检查目录权限**
   ```bash
   # Windows
   attrib uploads
   
   # 确保目录可写
   ```

2. **检查磁盘空间**
   ```bash
   # 查看磁盘剩余空间
   dir D:
   ```

3. **检查后端日志**
   - 查看详细的错误信息
   - 确认路径配置是否正确

4. **检查代理配置**
   - 确认 Vite 代理配置正确
   - 确认后端服务运行在 8082 端口

### Q4: 后台编辑保存失败

**排查方法：**

1. **检查浏览器网络请求**
   - 打开开发者工具
   - 查看 Network 标签页
   - 找到 PUT 请求 `/api/activities/{id}`
   - 查看 Response 中的错误信息

2. **检查后端日志**
   - 查看是否有字段映射错误
   - 确认实体类是否有对应字段

3. **验证数据库字段类型**
   ```sql
   DESCRIBE activities;
   ```
   - 确认新字段存在且类型正确（INT）

---

## 回滚方案

如果需要回滚数据库字段：

```sql
ALTER TABLE activities DROP COLUMN daily_candidate_limit;
ALTER TABLE activities DROP COLUMN candidate_daily_limit;
```

前端代码通过 Git 可以回滚到修改前的版本。

---

## 联系与支持

如果以上步骤都无法解决问题，请提供以下信息：

1. 后端日志（最近 100 行）
2. 浏览器控制台截图
3. 网络请求详情（F12 → Network）
4. 数据库字段查询结果
5. Redis 缓存键列表（如果使用 Redis）

---

## 文件清单

| 文件 | 用途 |
|------|------|
| `check_db_fields.sql` | 检查数据库字段是否存在 |
| `fix_upload_path.py` | 自动创建上传目录 |
| `manual_upgrade_guide.md` | 详细的升级指南 |
| `VOTE_LIMIT_UPGRADE.md` | 功能说明文档 |
| `FIX_GUIDE.md` | 本文件 - 综合修复指南 |

---

## 快速修复命令

```bash
# 1. 检查数据库字段
mysql -h localhost -P 3307 -u root -pmysql8123 dzvote_v2 < check_db_fields.sql

# 2. 如果字段不存在，执行升级
mysql -h localhost -P 3307 -u root -pmysql8123 dzvote_v2 <<EOF
ALTER TABLE activities ADD COLUMN daily_candidate_limit INT DEFAULT 5 COMMENT '每天最多对多少名候选人投票';
ALTER TABLE activities ADD COLUMN candidate_daily_limit INT DEFAULT 1 COMMENT '每个候选人每天限投多少次';
EOF

# 3. 编译后端
cd d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service
mvn clean compile -DskipTests

# 4. 重启服务
restart.bat

# 5. 创建上传目录
python fix_upload_path.py
```

完成后，刷新前端页面即可。
