-- 手动添加activities表缺失的字段
-- 请在MySQL客户端中执行：

USE dzvote_v2;

-- 添加缺失的列
ALTER TABLE activities
ADD COLUMN `subtitle` VARCHAR(200) COMMENT '副标题',
ADD COLUMN `region` VARCHAR(100) COMMENT '区域',
ADD COLUMN `cover_image` VARCHAR(500) COMMENT '封面图',
ADD COLUMN `group_count` INT DEFAULT 1 COMMENT '分组数量',
ADD COLUMN `sms_weight` INT DEFAULT 100 COMMENT '短信权重',
ADD COLUMN `ivr_weight` INT DEFAULT 100 COMMENT '语音权重',
ADD COLUMN `web_weight` INT DEFAULT 100 COMMENT '网络权重',
ADD COLUMN `app_weight` INT DEFAULT 100 COMMENT 'APP权重',
ADD COLUMN `wechat_weight` INT DEFAULT 100 COMMENT '微信权重',
ADD COLUMN `pay_weight` INT DEFAULT 100 COMMENT '付费权重',
ADD COLUMN `ip_limit` INT DEFAULT 0 COMMENT 'IP投票限制',
ADD COLUMN `enable_captcha` TINYINT DEFAULT 1 COMMENT '是否启用验证码',
ADD COLUMN `show_votes` TINYINT DEFAULT 1 COMMENT '是否显示票数',
ADD COLUMN `domain` VARCHAR(200) COMMENT '域名',
ADD COLUMN `template` VARCHAR(50) COMMENT '模板',
ADD COLUMN `rules` TEXT COMMENT '规则',
ADD COLUMN `service_id` VARCHAR(100) COMMENT '活动唯一ID',
ADD COLUMN `update_by` VARCHAR(50) COMMENT '更新人',
ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记';
