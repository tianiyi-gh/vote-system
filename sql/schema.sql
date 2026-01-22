-- DZVote v2.0 数据库表结构
-- 使用MySQL 8.0优化配置

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 活动表
CREATE TABLE IF NOT EXISTS `activities` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
    `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
    `description` TEXT COMMENT '活动描述',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-进行中，2-已结束，3-已取消',
    `vote_limit` INT DEFAULT 1 COMMENT '每人投票限制',
    `candidate_count` INT DEFAULT 0 COMMENT '候选人数量',
    `total_votes` INT DEFAULT 0 COMMENT '总票数',
    `created_by` VARCHAR(50) COMMENT '创建人',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_time` (`start_time`, `end_time`),
    INDEX `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 候选人表
CREATE TABLE IF NOT EXISTS `candidates` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '候选人ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `name` VARCHAR(100) NOT NULL COMMENT '候选人姓名',
    `description` TEXT COMMENT '候选人描述',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `votes` INT DEFAULT 0 COMMENT '得票数',
    `order_num` INT DEFAULT 0 COMMENT '排序号',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，2-禁用',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`activity_id`) REFERENCES `activities`(`id`) ON DELETE CASCADE,
    INDEX `idx_activity` (`activity_id`),
    INDEX `idx_votes` (`votes`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='候选人表';

-- 投票记录表
CREATE TABLE IF NOT EXISTS `vote_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '投票记录ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `candidate_id` BIGINT NOT NULL COMMENT '候选人ID',
    `voter_name` VARCHAR(100) COMMENT '投票人姓名',
    `voter_phone` VARCHAR(20) COMMENT '投票人手机号',
    `voter_ip` VARCHAR(45) COMMENT '投票人IP',
    `channel` VARCHAR(20) NOT NULL COMMENT '投票渠道：SMS/IVR/WEB/APP/WECHAT/PAY',
    `vote_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    `valid` TINYINT DEFAULT 1 COMMENT '是否有效：1-有效，0-无效',
    `device_info` TEXT COMMENT '设备信息',
    `location` VARCHAR(200) COMMENT '投票地点',
    FOREIGN KEY (`activity_id`) REFERENCES `activities`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`candidate_id`) REFERENCES `candidates`(`id`) ON DELETE CASCADE,
    INDEX `idx_activity` (`activity_id`),
    INDEX `idx_candidate` (`candidate_id`),
    INDEX `idx_phone` (`voter_phone`),
    INDEX `idx_time` (`vote_time`),
    INDEX `idx_channel` (`channel`),
    UNIQUE KEY `uniq_vote` (`activity_id`, `voter_phone`, `candidate_id`, `vote_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票记录表';

-- 投票限制表
CREATE TABLE IF NOT EXISTS `vote_limits` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '限制ID',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID',
    `voter_phone` VARCHAR(20) NOT NULL COMMENT '投票人手机号',
    `voter_ip` VARCHAR(45) COMMENT '投票人IP',
    `vote_count` INT DEFAULT 1 COMMENT '已投票数',
    `last_vote_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后投票时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`activity_id`) REFERENCES `activities`(`id`) ON DELETE CASCADE,
    INDEX `idx_activity_phone` (`activity_id`, `voter_phone`),
    INDEX `idx_activity_ip` (`activity_id`, `voter_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票限制表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(500) COMMENT '配置描述',
    `config_type` VARCHAR(20) DEFAULT 'STRING' COMMENT '配置类型：STRING/NUMBER/BOOLEAN/JSON',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(100) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `last_login_time` TIMESTAMP NULL COMMENT '最后登录时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `user_id` BIGINT COMMENT '操作用户ID',
    `username` VARCHAR(50) COMMENT '操作用户名',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `resource` VARCHAR(100) COMMENT '操作资源',
    `resource_id` VARCHAR(50) COMMENT '资源ID',
    `method` VARCHAR(10) COMMENT '请求方法',
    `url` VARCHAR(500) COMMENT '请求URL',
    `ip` VARCHAR(45) COMMENT '操作IP',
    `user_agent` TEXT COMMENT '用户代理',
    `request_params` TEXT COMMENT '请求参数',
    `response_result` TEXT COMMENT '响应结果',
    `execution_time` INT COMMENT '执行时间(ms)',
    `status` TINYINT DEFAULT 1 COMMENT '执行状态：1-成功，0-失败',
    `error_msg` TEXT COMMENT '错误信息',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX `idx_user` (`user_id`),
    INDEX `idx_operation` (`operation`),
    INDEX `idx_time` (`created_at`),
    INDEX `idx_ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 统计表
CREATE TABLE IF NOT EXISTS `statistics` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `activity_id` BIGINT COMMENT '活动ID',
    `total_votes` INT DEFAULT 0 COMMENT '总投票数',
    `valid_votes` INT DEFAULT 0 COMMENT '有效投票数',
    `invalid_votes` INT DEFAULT 0 COMMENT '无效投票数',
    `unique_voters` INT DEFAULT 0 COMMENT '独立投票人数',
    `peak_hour` TINYINT COMMENT '高峰时段(0-23)',
    `peak_votes` INT DEFAULT 0 COMMENT '高峰时段票数',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`activity_id`) REFERENCES `activities`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uniq_date_activity` (`stat_date`, `activity_id`),
    INDEX `idx_date` (`stat_date`),
    INDEX `idx_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统计表';

SET FOREIGN_KEY_CHECKS = 1;

-- 插入默认配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `config_type`) VALUES
('system.name', 'DZVote投票系统', '系统名称', 'STRING'),
('system.version', '2.0.0', '系统版本', 'STRING'),
('vote.max_per_day', '10', '每日最大投票数', 'NUMBER'),
('vote.enable_captcha', 'true', '是否启用验证码', 'BOOLEAN'),
('system.allow_multi_vote', 'false', '是否允许重复投票', 'BOOLEAN');