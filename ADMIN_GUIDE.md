# 管理后台使用指南

## 功能概述

管理后台提供完整的投票系统管理功能，包括活动管理、候选人管理、数据统计分析和防刷票设置。

## 访问地址

```
http://localhost/admin.html
```

## 功能模块

### 1. 活动管理

#### 查看活动列表
- 显示所有活动的ID、标题、时间、状态、候选人数、总票数
- 状态标识：🟢 进行中 | 🔴 已结束 | 🟡 已取消

#### 创建新活动
1. 点击 "+ 新建活动" 按钮
2. 填写活动信息：
   - 活动标题（必填）
   - 活动描述
   - 开始时间
   - 结束时间
   - 每人投票限制（默认1票，可设置1-10票）
   - 活动状态（进行中/已结束/已取消）
3. 点击"保存"完成创建

#### 编辑活动
1. 在活动列表中点击"编辑"按钮
2. 修改活动信息
3. 点击"保存"完成编辑

#### 删除活动
1. 在活动列表中点击"删除"按钮
2. 确认删除操作

**注意**: 删除活动会同时删除该活动下的所有候选人和投票记录。

### 2. 候选人管理

#### 选择活动
- 从下拉菜单选择要管理的活动
- 加载该活动的候选人列表

#### 查看候选人
- 显示候选人ID、姓名、头像、描述、票数、排序
- 支持手动调整票数

#### 创建新候选人
1. 选择活动后点击 "+ 新建候选人"
2. 点击图片区域上传头像（支持jpg、png等格式，最大2MB）
3. 填写候选人信息：
   - 姓名（必填）
   - 描述（最多30字，自动截断）
   - 排序号（数值越小越靠前）
4. 点击"保存"完成创建

#### 编辑候选人
1. 在候选人列表中点击"编辑"按钮
2. 修改候选人信息
3. 点击"保存"完成编辑

#### 删除候选人
1. 在候选人列表中点击"删除"按钮
2. 确认删除操作

#### 手动调整票数
1. 在候选人列表的"票数"列输入调整值（正数增加，负数减少）
2. 点击"调整"按钮
3. 票数立即更新

**注意**: 手动调整票数会影响活动总票数，但不会生成投票记录。

### 3. 统计分析

#### 查看活动概览
- 选择要统计的活动
- 自动显示：
  - 总票数
  - 独立投票人数
  - 候选人数

#### 候选人排名
- 显示按票数排序的候选人排名
- 前三名显示奖牌图标：🥇 🥈 🥉
- 显示每个候选人的得票占比

### 4. 防刷票设置

#### IP限制
- 设置同一IP在1小时内最多能投多少票
- 范围：1-10票
- 默认值：1票

#### 频率限制
- 设置每个IP每分钟最多请求次数
- 防止频繁请求攻击
- 默认值：10次

#### 验证码设置
- 启用/禁用投票前验证码
- 当前为预留功能，需后端实现

#### 用户限制
- 设置每个用户最多能投多少票
- 基于活动配置的vote_limit参数
- 默认值：1票

**注意**: 防刷票设置保存在浏览器localStorage中，需要重启服务才能生效。

## API接口文档

### 活动管理
```
GET    /api/admin/activities              # 获取所有活动
POST   /api/admin/activities              # 创建活动
PUT    /api/admin/activities/{id}          # 更新活动
DELETE /api/admin/activities/{id}          # 删除活动
```

### 候选人管理
```
GET    /api/admin/candidates             # 获取候选人列表（可按activityId筛选）
GET    /api/admin/candidates/{id}         # 获取候选人详情
POST   /api/admin/candidates             # 创建候选人
PUT    /api/admin/candidates/{id}         # 更新候选人
DELETE /api/admin/candidates/{id}         # 删除候选人
POST   /api/admin/candidates/{id}/votes/adjust  # 调整票数
```

### 文件上传
```
POST   /api/upload/avatar                # 上传候选人头像
DELETE /api/upload/file?filepath=xxx     # 删除文件
```

### 统计数据
```
GET    /api/admin/overview              # 获取管理后台概览统计
GET    /api/admin/activities/{id}/statistics  # 获取活动详细统计
```

## 数据库操作

### 直接修改投票限制
```sql
-- 查看所有活动的投票限制
SELECT id, title, vote_limit FROM activities;

-- 修改活动投票限制
UPDATE activities SET vote_limit = 5 WHERE id = 1;

-- 批量设置
UPDATE activities SET vote_limit = 10 WHERE vote_limit IS NULL;
```

### 清空投票记录
```sql
-- 清空所有投票记录
TRUNCATE TABLE vote_records;

-- 重置候选人票数
UPDATE candidates SET votes = 0;

-- 重置活动总票数
UPDATE activities SET total_votes = 0;

-- 清空Redis限流记录
redis-cli --scan --pattern 'vote:limit:*' | xargs redis-cli DEL
```

## 常见问题

### Q: 管理后台无法访问？
A: 确保vote-service正在运行（端口8082），检查防火墙设置。

### Q: 上传图片失败？
A:
1. 检查文件大小（不超过2MB）
2. 检查文件格式（必须是图片）
3. 检查uploads目录是否有写入权限

### Q: 票数调整不生效？
A:
1. 刷新页面查看最新数据
2. 检查后端服务是否正常运行
3. 查看浏览器控制台错误信息

### Q: 统计数据不准确？
A:
1. 确保投票记录表数据正确
2. 检查candidates表的votes字段
3. 检查activities表的total_votes字段

### Q: 如何导出数据？
A: 当前版本未提供导出功能，可以通过数据库直接查询：
```sql
-- 导出活动投票记录
SELECT * FROM vote_records WHERE activity_id = 1;

-- 导出候选人数据
SELECT * FROM candidates WHERE activity_id = 1;
```

## 安全建议

1. **设置管理员密码**: 当前版本未实现认证，生产环境请添加登录验证
2. **限制访问IP**: 配置Nginx或防火墙，只允许特定IP访问管理后台
3. **定期备份数据**: 定期备份MySQL数据库
4. **监控异常行为**: 关注投票日志，发现异常及时处理
5. **启用HTTPS**: 生产环境必须使用HTTPS

## 下一步计划

- [ ] 管理员登录认证
- [ ] 数据导出功能（Excel/CSV）
- [ ] 投票日志查看
- [ ] 图表展示（ECharts集成）
- [ ] 短信验证码
- [ ] IP黑名单管理
