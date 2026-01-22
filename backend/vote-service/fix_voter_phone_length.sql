-- 修复 voter_phone 字段长度问题
-- 问题：指纹ID user_1768551236064_f4ltdknev (30字符) 超过了 VARCHAR(20) 限制
-- 解决方案：将 voter_phone 字段扩展到 VARCHAR(100)

ALTER TABLE vote_records
MODIFY COLUMN voter_phone VARCHAR(100) COMMENT '投票人手机号或指纹ID';

-- 验证修改
DESCRIBE vote_records;
