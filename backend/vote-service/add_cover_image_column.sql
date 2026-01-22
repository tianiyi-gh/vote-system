-- 为 activities 表添加 cover_image 字段
ALTER TABLE `activities`
ADD COLUMN `cover_image` VARCHAR(500) COMMENT '封面图' AFTER `description`;

-- 同时添加其他缺失的字段
ALTER TABLE `activities`
ADD COLUMN `service_id` VARCHAR(100) COMMENT '活动唯一ID' AFTER `id`,
ADD COLUMN `subtitle` VARCHAR(200) COMMENT '副标题' AFTER `title`,
ADD COLUMN `region` VARCHAR(100) COMMENT '区域' AFTER `subtitle`,
ADD COLUMN `group_count` INT DEFAULT 1 COMMENT '分组数量' AFTER `status`,
ADD COLUMN `sms_weight` INT DEFAULT 100 COMMENT '短信权重' AFTER `group_count`,
ADD COLUMN `ivr_weight` INT DEFAULT 100 COMMENT '语音权重' AFTER `sms_weight`,
ADD COLUMN `web_weight` INT DEFAULT 100 COMMENT '网络权重' AFTER `ivr_weight`,
ADD COLUMN `app_weight` INT DEFAULT 100 COMMENT 'APP权重' AFTER `web_weight`,
ADD COLUMN `wechat_weight` INT DEFAULT 100 COMMENT '微信权重' AFTER `app_weight`,
ADD COLUMN `pay_weight` INT DEFAULT 100 COMMENT '付费权重' AFTER `wechat_weight`,
ADD COLUMN `ip_limit` INT DEFAULT 0 COMMENT 'IP限制' AFTER `pay_weight`,
ADD COLUMN `enable_captcha` TINYINT DEFAULT 1 COMMENT '是否启用验证码' AFTER `ip_limit`,
ADD COLUMN `show_votes` TINYINT DEFAULT 1 COMMENT '是否显示票数' AFTER `enable_captcha`,
ADD COLUMN `domain` VARCHAR(200) COMMENT '域名' AFTER `show_votes`,
ADD COLUMN `template` VARCHAR(100) COMMENT '模板' AFTER `domain`,
ADD COLUMN `rules` TEXT COMMENT '规则' AFTER `template`,
ADD COLUMN `update_by` VARCHAR(50) COMMENT '更新人' AFTER `created_at`,
ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记' AFTER `updated_at`;

-- 添加唯一索引
ALTER TABLE `activities`
ADD UNIQUE INDEX `idx_service_id` (`service_id`);
