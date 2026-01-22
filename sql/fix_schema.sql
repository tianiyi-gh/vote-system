-- 添加 deleted 字段到相关表

-- 活动表添加 deleted 字段
ALTER TABLE activities ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除' AFTER updated_at;

-- 用户表添加 deleted 字段
ALTER TABLE users ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除' AFTER updated_at;
