-- DZVote v2.0 初始化数据

-- 插入测试用户
INSERT INTO `users` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFD', '系统管理员', '13800138000', 'admin@dzvote.com', 'ADMIN', 1),
('test_user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFD', '测试用户', '13800138001', 'test@dzvote.com', 'USER', 1);

-- 插入示例活动
INSERT INTO `activities` (`title`, `description`, `start_time`, `end_time`, `status`, `vote_limit`, `created_by`) VALUES
('2024年度优秀员工评选', '评选公司年度优秀员工，每人可投3票', '2024-01-01 09:00:00', '2024-12-31 18:00:00', 1, 3, 'admin'),
('最佳团队投票', '选择最优秀的团队进行表彰', '2024-06-01 00:00:00', '2024-06-30 23:59:59', 2, 1, 'admin');

-- 插入候选人（针对第一个活动）
INSERT INTO `candidates` (`activity_id`, `name`, `description`, `avatar`, `votes`, `order_num`, `status`) VALUES
(1, '张三', '技术部高级工程师，工作认真负责，技术能力强', 'https://example.com/avatar1.jpg', 15, 1, 1),
(1, '李四', '销售部销售经理，业绩突出，团队协作好', 'https://example.com/avatar2.jpg', 12, 2, 1),
(1, '王五', '市场部产品经理，创新能力突出，执行力强', 'https://example.com/avatar3.jpg', 8, 3, 1),
(1, '赵六', '人事部主管，管理能力强，团队建设好', 'https://example.com/avatar4.jpg', 6, 4, 1);

-- 插入候选人（针对第二个活动）
INSERT INTO `candidates` (`activity_id`, `name`, `description`, `avatar`, `votes`, `order_num`, `status`) VALUES
(2, '技术团队', '技术部团队，技术实力强，项目交付准时', 'https://example.com/team1.jpg', 25, 1, 1),
(2, '销售团队', '销售部团队，业绩优秀，客户满意度高', 'https://example.com/team2.jpg', 22, 2, 1),
(2, '市场团队', '市场部团队，营销能力强，品牌影响力大', 'https://example.com/team3.jpg', 18, 3, 1);

-- 插入投票记录示例
INSERT INTO `vote_records` (`activity_id`, `candidate_id`, `voter_name`, `voter_phone`, `voter_ip`, `channel`, `vote_time`, `valid`) VALUES
(1, 1, '刘明', '13800138010', '192.168.1.100', 'WEB', '2024-12-15 10:30:00', 1),
(1, 1, '陈芳', '13800138011', '192.168.1.101', 'APP', '2024-12-15 11:20:00', 1),
(1, 2, '王强', '13800138012', '192.168.1.102', 'WEB', '2024-12-15 14:15:00', 1),
(1, 3, '赵丽', '13800138013', '192.168.1.103', 'WECHAT', '2024-12-15 16:45:00', 1),
(1, 1, '孙伟', '13800138014', '192.168.1.104', 'SMS', '2024-12-16 09:30:00', 1);

-- 更新活动候选人数量和总票数
UPDATE `activities` SET 
    candidate_count = (SELECT COUNT(*) FROM `candidates` WHERE `activity_id` = 1),
    total_votes = (SELECT SUM(votes) FROM `candidates` WHERE `activity_id` = 1)
WHERE `id` = 1;

UPDATE `activities` SET 
    candidate_count = (SELECT COUNT(*) FROM `candidates` WHERE `activity_id` = 2),
    total_votes = (SELECT SUM(votes) FROM `candidates` WHERE `activity_id` = 2)
WHERE `id` = 2;

-- 插入统计数据
INSERT INTO `statistics` (`stat_date`, `activity_id`, `total_votes`, `valid_votes`, `invalid_votes`, `unique_voters`, `peak_hour`, `peak_votes`) VALUES
('2024-12-15', 1, 4, 4, 0, 4, 16, 1),
('2024-12-16', 1, 1, 1, 0, 1, 9, 1),
('2024-12-15', 2, 0, 0, 0, 0, NULL, 0);

-- 插入操作日志
INSERT INTO `operation_logs` (`user_id`, `username`, `operation`, `resource`, `method`, `url`, `ip`, `status`, `created_at`) VALUES
(1, 'admin', 'CREATE', 'activity', 'POST', '/api/activities', '127.0.0.1', 1, '2024-12-15 08:30:00'),
(1, 'admin', 'CREATE', 'candidate', 'POST', '/api/candidates', '127.0.0.1', 1, '2024-12-15 08:45:00'),
(2, 'test_user', 'VOTE', 'vote', 'POST', '/api/votes', '192.168.1.100', 1, '2024-12-15 10:30:00');

-- 更新系统配置
UPDATE `system_config` SET `config_value` = 'DZVote v2.0 - 高性能投票系统' WHERE `config_key` = 'system.name';
INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `config_type`) VALUES
('vote.channels', 'SMS,IVR,WEB,APP,WECHAT,PAY', '支持的投票渠道', 'STRING'),
('system.timezone', 'Asia/Shanghai', '系统时区', 'STRING'),
('system.max_upload_size', '10485760', '最大上传文件大小(字节)', 'NUMBER');