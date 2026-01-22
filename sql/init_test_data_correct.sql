-- DZVote v2.0 测试数据初始化
-- 使用正确的表名：activities, candidates, vote_user (或 users)

USE `dzvote_v2`;

-- 插入测试用户 (如果使用 users 表)
INSERT INTO `users` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`, `created_at`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFD', '系统管理员', '13800138000', 'admin@dzvote.com', 'ADMIN', 1, NOW()),
('test_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFD', '测试用户', '13800138001', 'test@dzvote.com', 'USER', 1, NOW())
ON DUPLICATE KEY UPDATE `username`=VALUES(`username`);

-- 如果使用 vote_user 表
-- INSERT INTO `vote_user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`, `created_at`) VALUES
-- ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFD', '系统管理员', '13800138000', 'admin@dzvote.com', 'ADMIN', 1, NOW()),
-- ('test_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFD', '测试用户', '13800138001', 'test@dzvote.com', 'USER', 1, NOW())
-- ON DUPLICATE KEY UPDATE `username`=VALUES(`username`);

-- 插入示例活动
INSERT INTO `activities` (`title`, `description`, `start_time`, `end_time`, `status`, `vote_limit`, `created_by`, `created_at`) VALUES
('2024年度优秀员工评选', '评选公司年度优秀员工，每人可投3票，公平公正公开', '2024-01-01 09:00:00', '2025-12-31 18:00:00', 1, 3, 'admin', NOW()),
('最佳团队投票', '选择最优秀的团队进行表彰，展现团队协作精神', '2024-06-01 00:00:00', '2025-06-30 23:59:59', 1, 1, 'admin', NOW())
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`);

-- 插入候选人（针对第一个活动）
INSERT INTO `candidates` (`activity_id`, `name`, `description`, `avatar`, `votes`, `order_num`, `status`, `created_at`) VALUES
(1, '张三', '技术部高级工程师，工作认真负责，技术能力强，主导多个核心项目开发', 'https://via.placeholder.com/150/4CAF50/ffffff?text=张三', 15, 1, 1, NOW()),
(1, '李四', '销售部销售经理，业绩突出，团队协作好，连续三年销售冠军', 'https://via.placeholder.com/150/2196F3/ffffff?text=李四', 12, 2, 1, NOW()),
(1, '王五', '市场部产品经理，创新能力突出，执行力强，成功推出多款明星产品', 'https://via.placeholder.com/150/FF9800/ffffff?text=王五', 8, 3, 1, NOW()),
(1, '赵六', '人事部主管，管理能力强，团队建设好，员工满意度高', 'https://via.placeholder.com/150/E91E63/ffffff?text=赵六', 6, 4, 1, NOW())
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 插入候选人（针对第二个活动）
INSERT INTO `candidates` (`activity_id`, `name`, `description`, `avatar`, `votes`, `order_num`, `status`, `created_at`) VALUES
(2, '技术团队', '技术部团队，技术实力强，项目交付准时，创新能力强', 'https://via.placeholder.com/150/4CAF50/ffffff?text=技术团队', 25, 1, 1, NOW()),
(2, '销售团队', '销售部团队，业绩优秀，客户满意度高，市场份额领先', 'https://via.placeholder.com/150/2196F3/ffffff?text=销售团队', 22, 2, 1, NOW()),
(2, '市场团队', '市场部团队，营销能力强，品牌影响力大，策划多场成功活动', 'https://via.placeholder.com/150/FF9800/ffffff?text=市场团队', 18, 3, 1, NOW())
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 更新活动候选人数量和总票数
UPDATE `activities` SET
    `candidate_count` = (SELECT COUNT(*) FROM `candidates` WHERE `activity_id` = 1),
    `total_votes` = (SELECT SUM(`votes`) FROM `candidates` WHERE `activity_id` = 1)
WHERE `id` = 1;

UPDATE `activities` SET
    `candidate_count` = (SELECT COUNT(*) FROM `candidates` WHERE `activity_id` = 2),
    `total_votes` = (SELECT SUM(`votes`) FROM `candidates` WHERE `activity_id` = 2)
WHERE `id` = 2;

-- 插入投票记录示例（如果表存在）
-- INSERT INTO `vote_records` (`activity_id`, `candidate_id`, `voter_name`, `voter_phone`, `voter_ip`, `channel`, `vote_time`, `valid`) VALUES
-- (1, 1, '刘明', '13800138010', '192.168.1.100', 'WEB', '2024-12-15 10:30:00', 1),
-- (1, 1, '陈芳', '13800138011', '192.168.1.101', 'APP', '2024-12-15 11:20:00', 1),
-- (1, 2, '王强', '13800138012', '192.168.1.102', 'WEB', '2024-12-15 14:15:00', 1),
-- (1, 3, '赵丽', '13800138013', '192.168.1.103', 'WECHAT', '2024-12-15 16:45:00', 1),
-- (1, 1, '孙伟', '13800138014', '192.168.1.104', 'SMS', '2024-12-16 09:30:00', 1);

SELECT '测试数据初始化完成！' AS message;
SELECT '活动列表：' AS info;
SELECT id, title, status, candidate_count, total_votes FROM `activities`;
SELECT '候选人列表：' AS info;
SELECT id, activity_id, name, votes, status FROM `candidates`;
