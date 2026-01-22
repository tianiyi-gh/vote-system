-- 检查投票限制相关字段
-- 执行时间：2026-01-15

-- 1. 检查 activities 表结构
DESCRIBE activities;

-- 2. 检查 daily_candidate_limit 字段
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'dzvote_v2'
  AND TABLE_NAME = 'activities'
  AND COLUMN_NAME IN ('vote_limit', 'daily_candidate_limit', 'candidate_daily_limit');

-- 3. 查看现有活动的限制设置
SELECT 
    id,
    title,
    vote_limit,
    daily_candidate_limit,
    candidate_daily_limit
FROM activities
LIMIT 5;
