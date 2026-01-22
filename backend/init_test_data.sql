-- 初始化测试数据

-- 修复 users 表，添加 deleted 字段（可选）
ALTER TABLE users ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记' AFTER updated_at;

-- 插入测试用户
INSERT INTO users (username, password, real_name, phone, email, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuQjBkKqO8qR3aP5JbRkZK8F', '管理员', '13800138000', 'admin@example.com', 'ADMIN', 1);

-- 插入测试活动
INSERT INTO activities (id, name, description, start_time, end_time, status) VALUES
(1, '测试投票活动', '这是一个测试投票活动', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE');

-- 插入测试候选人
INSERT INTO candidates (id, activity_id, name, description, photo_url, vote_count, status) VALUES
(1, 1, '候选人1', '第一个候选人', 'http://example.com/photo1.jpg', 0, 'ACTIVE'),
(2, 1, '候选人2', '第二个候选人', 'http://example.com/photo2.jpg', 0, 'ACTIVE');
