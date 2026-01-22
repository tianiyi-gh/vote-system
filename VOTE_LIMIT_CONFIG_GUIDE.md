# 活动投票限制配置指南

## 功能说明
现在系统支持针对每个活动单独配置投票限制，每个活动可以设置不同的每人投票次数限制。

## 数据库配置

### 方法1: 直接修改数据库
使用 SQL 语句更新活动的 `vote_limit` 字段：

```sql
-- 格式
UPDATE activities SET vote_limit = <数量> WHERE id = <活动ID>;

-- 示例: 设置活动ID为1的活动每人可投5票
UPDATE activities SET vote_limit = 5 WHERE id = 1;

-- 示例: 设置活动ID为2的活动每人可投10票
UPDATE activities SET vote_limit = 10 WHERE id = 2;
```

### 方法2: 通过Docker命令
```bash
# 查看所有活动的投票限制配置
docker exec dzvote-mysql8 sh -c "mysql -uroot -pmysql8123 dzvote_v2 -e 'SELECT id, title, vote_limit FROM activities;'"

# 更新活动投票限制
docker exec dzvote-mysql8 sh -c "mysql -uroot -pmysql8123 dzvote_v2 -e 'UPDATE activities SET vote_limit = 5 WHERE id = 1;'"
```

### 方法3: 使用批量更新脚本
```bash
# 执行批量更新脚本
docker exec dzvote-mysql8 sh -c "mysql -uroot -pmysql8123 dzvote_v2" < update_vote_limits.sql
```

## 当前活动配置

| 活动ID | 活动标题 | 每人投票限制 | 状态 |
|--------|----------|--------------|------|
| 1 | 2024年度优秀员工评选 | 3票 | 进行中 |
| 2 | 测试活动2 | 10票 | 进行中 |
| 3 | 2026年度评选活动 | 10票 | 进行中 |

## 技术实现

### 后端实现
- **VoteServiceImpl.java**: 从活动表中读取 `vote_limit` 字段
- **ActivityMapper.java**: 提供查询活动信息的接口
- **Redis限流**: 使用 Redis 存储每个IP在每个活动中的投票计数

### 前端实现
- **vote.html**: 自动读取活动的 `vote_limit` 配置
- 动态显示剩余投票次数
- 根据配置限制投票操作

### 限流机制
- 每个IP每天在每个活动中的投票次数限制
- Redis Key 格式: `vote:limit:ip:<activityId>:<ipAddress>`
- 过期时间: 1天

## API示例

### 获取活动列表（包含投票限制）
```bash
GET http://localhost:8082/api/activities
```

返回数据包含 `voteLimit` 字段：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "title": "2024年度优秀员工评选",
      "voteLimit": 3,
      "status": 1
    }
  ]
}
```

### 投票时自动应用限制
```bash
POST http://localhost:8082/api/votes
Content-Type: application/json

{
  "activityId": 1,
  "candidateId": 100,
  "channel": "WEB",
  "voterIp": "127.0.0.1"
}
```

系统会自动检查该活动配置的投票限制，超过限制则返回错误。

## 常见问题

### Q: 如何临时清除某个IP的投票限制？
```bash
# 清除 Redis 中的限流记录
redis-cli DEL "vote:limit:ip:1:127.0.0.1"
```

### Q: 活动结束后如何调整投票限制？
活动结束后（status=2），投票接口仍然会被验证，但可以在后台将活动状态改为已结束。

### Q: 默认投票限制是多少？
如果没有设置 `vote_limit` 字段，默认值为10票。

### Q: 如何批量设置多个活动的投票限制？
可以使用 update_vote_limits.sql 脚本批量更新，或编写循环SQL语句。

## 注意事项

1. **数据一致性**: 修改活动投票限制后，需要重启 vote-service 服务以刷新内存缓存
2. **Redis清理**: 如果修改了投票限制，可能需要清理相关的 Redis 缓存
3. **用户体验**: 前端会自动显示当前的投票限制和剩余投票次数
4. **活动状态**: 只有进行中（status=1）的活动才能接受投票
