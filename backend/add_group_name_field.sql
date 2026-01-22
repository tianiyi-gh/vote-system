-- 添加候选人分组字段到 candidates 表
-- 如果字段已存在，忽略错误
ALTER TABLE candidates ADD COLUMN group_name VARCHAR(50) DEFAULT '1' COMMENT '分组名称:1-第一组,2-第二组,3-第三组';
