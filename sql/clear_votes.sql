-- 清空投票记录和重置票数

-- 1. 清空投票记录表
TRUNCATE TABLE vote_records;

-- 2. 重置候选人的票数为0
UPDATE candidates SET votes = 0;

-- 3. 重置活动的总票数
UPDATE activities SET total_votes = 0;

-- 4. 清空投票限制表
TRUNCATE TABLE vote_limits;

-- 5. 显示结果
SELECT 
    (SELECT COUNT(*) FROM vote_records) as vote_records_count,
    (SELECT COUNT(*) FROM candidates) as candidates_count,
    (SELECT COUNT(*) FROM activities) as activities_count;
