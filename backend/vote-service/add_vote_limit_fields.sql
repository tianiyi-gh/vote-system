-- 添加新的投票限制字段到 activities 表
-- 执行时间：2026-01-15

ALTER TABLE activities
ADD COLUMN daily_candidate_limit INT DEFAULT 5 COMMENT '每天最多对多少名候选人投票（例如：5），0表示不限制';

ALTER TABLE activities
ADD COLUMN candidate_daily_limit INT DEFAULT 1 COMMENT '每个候选人每天限投多少次（例如：1），0表示不限制';

-- 为现有活动设置默认值
UPDATE activities SET daily_candidate_limit = 5 WHERE daily_candidate_limit IS NULL;
UPDATE activities SET candidate_daily_limit = 1 WHERE candidate_daily_limit IS NULL;

-- 验证字段是否添加成功
SELECT
    id,
    title,
    vote_limit,
    daily_candidate_limit,
    candidate_daily_limit
FROM activities
LIMIT 5;
