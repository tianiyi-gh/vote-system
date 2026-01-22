@echo off
chcp 65001 >nul
echo 正在初始化测试数据...

REM 使用Docker连接到MySQL容器
docker exec -i dzvote-mysql8 mysql -uroot -pmysql8123 dzvote_v2 << EOF
-- 插入测试活动
INSERT INTO activities (title, description, start_time, end_time, status, vote_limit, created_by, created_at, deleted)
VALUES ('2024年度优秀员工评选', '评选公司年度优秀员工，每人可投3票', '2024-01-01 09:00:00', '2025-12-31 18:00:00', 1, 3, 'admin', NOW(), 0)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 插入候选人
INSERT INTO candidates (activity_id, name, description, avatar, votes, order_num, status, created_at) VALUES
(1, '张三', '技术部高级工程师，工作认真负责', 'https://via.placeholder.com/150/4CAF50/ffffff?text=张三', 15, 1, 1, NOW()),
(1, '李四', '销售部销售经理，业绩突出', 'https://via.placeholder.com/150/2196F3/ffffff?text=李四', 12, 2, 1, NOW()),
(1, '王五', '市场部产品经理，创新能力突出', 'https://via.placeholder.com/150/FF9800/ffffff?text=王五', 8, 3, 1, NOW()),
(1, '赵六', '人事部主管，管理能力强', 'https://via.placeholder.com/150/E91E63/ffffff?text=赵六', 6, 4, 1, NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 更新活动统计
UPDATE activities SET
    candidate_count = (SELECT COUNT(*) FROM candidates WHERE activity_id = 1 AND status = 1),
    total_votes = (SELECT SUM(votes) FROM candidates WHERE activity_id = 1 AND status = 1)
WHERE id = 1;

-- 显示结果
SELECT '测试数据初始化完成！' AS message;
SELECT id, title, status, candidate_count, total_votes FROM activities WHERE id = 1;
SELECT id, name, votes FROM candidates WHERE activity_id = 1;
EOF

echo.
echo ✅ 测试数据初始化完成！
pause
