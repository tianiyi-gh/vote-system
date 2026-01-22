#!/bin/bash
docker exec -i dzvote-mysql8 mysql -u root -pmysql8123 dzvote_v2 << 'EOF'
-- 添加候选人
INSERT INTO candidates (id, activity_id, name, description, photo_url, vote_count, status) VALUES
(1, 1, '候选人1', '第一个候选人', 'http://example.com/photo1.jpg', 0, 'ACTIVE'),
(2, 1, '候选人2', '第二个候选人', 'http://example.com/photo2.jpg', 0, 'ACTIVE');

-- 添加用户 (如果没有admin)
INSERT IGNORE INTO users (username, password, real_name, phone, email, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuQjBkKqO8qR3aP5JbRkZK8F', '管理员', '13800138000', 'admin@example.com', 'ADMIN', 1);

EOF
