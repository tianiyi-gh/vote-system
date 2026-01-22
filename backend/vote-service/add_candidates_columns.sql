-- 为 candidates 表添加缺失字段
ALTER TABLE `candidates`
ADD COLUMN `service_id` VARCHAR(100) COMMENT '活动Service ID' AFTER `activity_id`,
ADD COLUMN `candidate_no` VARCHAR(50) COMMENT '候选人编号' AFTER `service_id`,
ADD COLUMN `group_name` VARCHAR(100) COMMENT '分组名称' AFTER `name`,
ADD COLUMN `photo` VARCHAR(500) COMMENT '照片URL' AFTER `description`,
ADD COLUMN `sms_votes` INT DEFAULT 0 COMMENT '短信票数' AFTER `votes`,
ADD COLUMN `ivr_votes` INT DEFAULT 0 COMMENT '语音票数' AFTER `sms_votes`,
ADD COLUMN `web_votes` INT DEFAULT 0 COMMENT '网络票数' AFTER `ivr_votes`,
ADD COLUMN `app_votes` INT DEFAULT 0 COMMENT 'APP票数' AFTER `web_votes`,
ADD COLUMN `wechat_votes` INT DEFAULT 0 COMMENT '微信票数' AFTER `app_votes`,
ADD COLUMN `pay_votes` INT DEFAULT 0 COMMENT '付费票数' AFTER `wechat_votes`,
ADD COLUMN `total_votes` INT DEFAULT 0 COMMENT '总票数' AFTER `pay_votes`,
ADD COLUMN `score` DECIMAL(10,2) DEFAULT 0.00 COMMENT '得分' AFTER `total_votes`,
ADD COLUMN `ranking` INT DEFAULT 0 COMMENT '排名' AFTER `score`,
ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记' AFTER `updated_at`;

-- 修改status字段类型和默认值
ALTER TABLE `candidates`
MODIFY COLUMN `status` TINYINT DEFAULT 0 COMMENT '状态：0-正常，1-禁用';

-- 修改votes字段兼容性（保留旧数据）
UPDATE `candidates`
SET `total_votes` = `votes`
WHERE `total_votes` = 0 AND `votes` > 0;

-- 添加索引
ALTER TABLE `candidates`
ADD INDEX `idx_service_id` (`service_id`),
ADD INDEX `idx_candidate_no` (`candidate_no`),
ADD INDEX `idx_deleted` (`deleted`);
