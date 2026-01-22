-- 更新活动投票限制
-- 格式: UPDATE activities SET vote_limit = <数量> WHERE id = <活动ID>;

-- 示例1: 活动ID 1 - 每人可投3票
UPDATE activities SET vote_limit = 3 WHERE id = 1;

-- 示例2: 活动ID 2 - 每人可投5票
UPDATE activities SET vote_limit = 5 WHERE id = 2;

-- 示例3: 活动ID 3 - 每人可投10票（默认值）
UPDATE activities SET vote_limit = 10 WHERE id = 3;

-- 查看所有活动的投票限制配置
SELECT
    id AS '活动ID',
    title AS '活动标题',
    vote_limit AS '每人投票限制',
    status AS '状态(1:进行中,2:已结束,3:已取消)',
    total_votes AS '总票数'
FROM activities
ORDER BY id;
