# 投票限制功能升级说明

## 更新时间
2026-01-15

## 功能概述
为投票系统添加了更细粒度的投票限制规则，支持以下新限制：

1. **每天最多对 N 名候选人投票**（例如：每天最多对 5 名候选人投票）
2. **每个候选人每天限投 N 次**（例如：每个候选人每天限投 1 次）

## 数据库变更

### 1. 新增字段到 activities 表

```sql
ALTER TABLE activities
ADD COLUMN daily_candidate_limit INT DEFAULT 5
COMMENT '每天最多对多少名候选人投票（例如：5），0表示不限制';

ALTER TABLE activities
ADD COLUMN candidate_daily_limit INT DEFAULT 1
COMMENT '每个候选人每天限投多少次（例如：1），0表示不限制';
```

### 2. 执行 SQL 脚本

```bash
mysql -u root -p your_database < add_vote_limit_fields.sql
```

或手动执行文件：
```
d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service\add_vote_limit_fields.sql
```

## 代码变更

### 1. Activity 实体类
**文件路径**: `vote-service/src/main/java/com/dzvote/vote/entity/Activity.java`

新增字段：
```java
/**
 * 每天最多对多少名候选人投票（例如：5）
 * 0表示不限制
 */
private Integer dailyCandidateLimit;

/**
 * 每个候选人每天限投多少次（例如：1）
 * 0表示不限制
 */
private Integer candidateDailyLimit;
```

### 2. VoteRecordMapper 接口
**文件路径**: `vote-service/src/main/java/com/dzvote/vote/mapper/VoteRecordMapper.java`

新增方法：
```java
/**
 * 统计用户今天对多少个不同候选人投过票
 */
@Select("SELECT COUNT(DISTINCT candidate_id) FROM vote_records " +
        "WHERE activity_id = #{activityId} " +
        "AND (voter_phone = #{voterPhone} OR voter_ip = #{voterIp}) " +
        "AND DATE(vote_time) = CURDATE() " +
        "AND valid = 1")
Integer countTodayVotedCandidates(@Param("activityId") Long activityId,
                                  @Param("voterPhone") String voterPhone,
                                  @Param("voterIp") String voterIp);

/**
 * 统计用户今天对某个候选人投了多少票
 */
@Select("SELECT COUNT(*) FROM vote_records " +
        "WHERE activity_id = #{activityId} " +
        "AND candidate_id = #{candidateId} " +
        "AND (voter_phone = #{voterPhone} OR voter_ip = #{voterIp}) " +
        "AND DATE(vote_time) = CURDATE() " +
        "AND valid = 1")
Integer countTodayVotesForCandidate(@Param("activityId") Long activityId,
                                     @Param("candidateId") Long candidateId,
                                     @Param("voterPhone") String voterPhone,
                                     @Param("voterIp") String voterIp);
```

### 3. VoteServiceImpl 服务类
**文件路径**: `vote-service/src/main/java/com/dzvote/vote/service/impl/VoteServiceImpl.java`

#### 新增检查方法

```java
/**
 * 检查每天最多对多少个候选人投票的限制
 */
private boolean checkDailyCandidateLimit(VoteRequest request, Integer dailyCandidateLimit) {
    if (dailyCandidateLimit == null || dailyCandidateLimit <= 0) {
        return true; // 未设置限制
    }

    // 统计用户今天对多少个不同候选人投过票
    Integer votedCount = voteRecordMapper.countTodayVotedCandidates(
        request.getActivityId(),
        request.getVoterPhone(),
        request.getVoterIp()
    );

    if (votedCount != null && votedCount >= dailyCandidateLimit) {
        log.info("每天最多候选人限制: activityId={}, voterPhone={}, votedCount={}, limit={}",
                request.getActivityId(), request.getVoterPhone(), votedCount, dailyCandidateLimit);
        return false;
    }

    return true;
}

/**
 * 检查每个候选人每天限投多少次的限制
 */
private boolean checkCandidateDailyLimit(VoteRequest request, Integer candidateDailyLimit) {
    if (candidateDailyLimit == null || candidateDailyLimit <= 0) {
        return true; // 未设置限制
    }

    // 统计用户今天对该候选人投了多少票
    Integer voteCount = voteRecordMapper.countTodayVotesForCandidate(
        request.getActivityId(),
        request.getCandidateId(),
        request.getVoterPhone(),
        request.getVoterIp()
    );

    if (voteCount != null && voteCount >= candidateDailyLimit) {
        log.info("候选人每天限投限制: activityId={}, candidateId={}, voterPhone={}, voteCount={}, limit={}",
                request.getActivityId(), request.getCandidateId(), request.getVoterPhone(), voteCount, candidateDailyLimit);
        return false;
    }

    return true;
}
```

#### 投票流程更新

在 `vote()` 方法中新增两个检查步骤：

```java
// 3.5. 检查每天最多对多少个候选人投票的限制
if (!checkDailyCandidateLimit(request, activity.getDailyCandidateLimit())) {
    return VoteResult.failure("DAILY_CANDIDATE_LIMIT_EXCEEDED",
        "您今天已达到最多可投候选人数限制（每天最多对" + activity.getDailyCandidateLimit() + "名候选人投票）");
}

// 3.6. 检查每个候选人每天限投多少次的限制
if (!checkCandidateDailyLimit(request, activity.getCandidateDailyLimit())) {
    return VoteResult.failure("CANDIDATE_DAILY_LIMIT_EXCEEDED",
        "您今天对该候选人的投票次数已达到限制（每个候选人每天限投" + activity.getCandidateDailyLimit() + "次）");
}
```

## 使用示例

### 配置活动限制规则

```sql
-- 设置活动ID为1的限制规则
UPDATE activities
SET
    vote_limit = 10,              -- 每人最多投10票（总票数限制）
    daily_candidate_limit = 5,     -- 每天最多对5名候选人投票
    candidate_daily_limit = 1     -- 每个候选人每天限投1次
WHERE id = 1;
```

### 配置说明

| 字段 | 说明 | 示例值 | 0值含义 |
|-----|------|-------|---------|
| vote_limit | 每人总票数限制 | 10 | 不限制总票数 |
| daily_candidate_limit | 每天最多对多少名候选人投票 | 5 | 不限制候选人数量 |
| candidate_daily_limit | 每个候选人每天限投多少次 | 1 | 不限制单个候选人投票次数 |

### 典型配置方案

#### 方案1：严格限制（防止刷票）
```sql
SET vote_limit = 5;
SET daily_candidate_limit = 3;
SET candidate_daily_limit = 1;
```
含义：每人最多5票，每天最多对3名候选人投票，每个候选人每天只能投1次。

#### 方案2：宽松限制（鼓励参与）
```sql
SET vote_limit = 20;
SET daily_candidate_limit = 10;
SET candidate_daily_limit = 5;
```
含义：每人最多20票，每天最多对10名候选人投票，每个候选人每天最多投5次。

#### 方案3：不限制（完全开放）
```sql
SET vote_limit = 0;
SET daily_candidate_limit = 0;
SET candidate_daily_limit = 0;
```
含义：不限制任何投票次数（不推荐用于正式活动）。

## 部署步骤

1. **备份数据库**
```bash
mysqldump -u root -p your_database > backup_$(date +%Y%m%d).sql
```

2. **执行数据库升级脚本**
```bash
mysql -u root -p your_database < add_vote_limit_fields.sql
```

3. **编译后端代码**
```bash
cd d:\ide\toupiao\ROOT_CodeBuddyCN\新架构\backend\vote-service
mvn clean compile -DskipTests
```

4. **重启后端服务**
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

5. **验证部署**
```bash
# 检查服务状态
curl http://localhost:8080/api/votes/health

# 预期返回：{"code":200,"message":"success","data":"Vote Service is running"}
```

## 验证测试

### 测试场景1：每天最多对5名候选人投票

1. 准备活动：设置 `daily_candidate_limit = 5`
2. 用户对5个不同候选人各投1票 → ✅ 成功
3. 用户尝试对第6个候选人投票 → ❌ 失败，提示"您今天已达到最多可投候选人数限制"

### 测试场景2：每个候选人每天限投1次

1. 准备活动：设置 `candidate_daily_limit = 1`
2. 用户对候选人A投1票 → ✅ 成功
3. 用户再次对候选人A投票 → ❌ 失败，提示"您今天对该候选人的投票次数已达到限制"

### 测试场景3：组合限制

1. 准备活动：
   - `vote_limit = 10`
   - `daily_candidate_limit = 5`
   - `candidate_daily_limit = 2`

2. 用户对5个候选人各投2票 → ✅ 成功（总共10票）
3. 用户尝试对第6个候选人投票 → ❌ 失败（超过每天最多5名候选人）

## 返回的错误码

| 错误码 | 说明 | 解决方法 |
|-------|------|---------|
| PERSON_LIMIT_EXCEEDED | 超过每人总票数限制 | 等待明天或联系管理员 |
| DAILY_CANDIDATE_LIMIT_EXCEEDED | 超过每天最多候选人数量 | 等待明天再投其他候选人 |
| CANDIDATE_DAILY_LIMIT_EXCEEDED | 超过单个候选人每日限制 | 等待明天再投该候选人 |
| IP_LIMIT_EXCEEDED | 超过IP投票限制 | 更换网络或等待重置 |

## 注意事项

1. **限制基于日期**：每天0点重置，统计基于 `CURDATE()`
2. **用户识别**：使用 `voter_phone` 和 `voter_ip` 联合识别，防止同一设备通过清缓存绕过
3. **兼容性**：
   - 字段默认值确保已有活动自动应用新规则
   - 字段为 `null` 或 `<= 0` 时不限制，保持向后兼容
4. **性能优化**：添加了必要的数据库索引（`activity_id`、`voter_phone`、`voter_ip`、`vote_time`）
5. **日志记录**：所有限制触发都会记录详细日志，便于审计

## 常见问题

### Q1: 如何查看某个活动的当前限制配置？
```sql
SELECT
    id,
    title,
    vote_limit,
    daily_candidate_limit,
    candidate_daily_limit
FROM activities
WHERE id = 1;
```

### Q2: 如何临时关闭某个限制？
将对应字段设置为 `0` 即可：
```sql
UPDATE activities SET candidate_daily_limit = 0 WHERE id = 1;
```

### Q3: 统计粒度是基于什么？
- 日期：服务器时区的自然日（00:00:00 - 23:59:59）
- 用户：手机号或IP（取其一或联合识别）

### Q4: 是否支持实时查看用户已投票数？
可以通过数据库查询：
```sql
-- 查看用户今天对多少个不同候选人投过票
SELECT COUNT(DISTINCT candidate_id) as voted_candidates
FROM vote_records
WHERE activity_id = 1
  AND voter_phone = '13800138000'
  AND DATE(vote_time) = CURDATE()
  AND valid = 1;

-- 查看用户今天对某个候选人投了多少票
SELECT COUNT(*) as vote_count
FROM vote_records
WHERE activity_id = 1
  AND candidate_id = 5
  AND voter_phone = '13800138000'
  AND DATE(vote_time) = CURDATE()
  AND valid = 1;
```

## 版本历史

- **v1.0** (2026-01-15): 初始版本，支持基本投票限制
- **v1.1** (2026-01-15): 新增细粒度限制规则
  - 每天最多对 N 名候选人投票
  - 每个候选人每天限投 N 次

## 相关文件

- 数据库脚本：`vote-service/add_vote_limit_fields.sql`
- 实体类：`vote-service/src/main/java/com/dzvote/vote/entity/Activity.java`
- Mapper接口：`vote-service/src/main/java/com/dzvote/vote/mapper/VoteRecordMapper.java`
- 服务实现：`vote-service/src/main/java/com/dzvote/vote/service/impl/VoteServiceImpl.java`
