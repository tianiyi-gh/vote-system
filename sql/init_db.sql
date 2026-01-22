-- ================================
-- DZVOTE 2.0 Database Initialization
-- Execute: mysql -u root -p dzvote_v2 < init_db.sql
-- ================================

USE `dzvote_v2`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ================================
-- 1. Vote Activity Table
-- ================================
DROP TABLE IF EXISTS `vote_activity`;
CREATE TABLE `vote_activity` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `service_id` VARCHAR(64) NOT NULL COMMENT 'Activity Unique ID',
  `title` VARCHAR(200) NOT NULL COMMENT 'Activity Title',
  `subtitle` VARCHAR(200) DEFAULT NULL COMMENT 'Subtitle',
  `region` VARCHAR(50) DEFAULT NULL COMMENT 'Region',
  `description` TEXT COMMENT 'Description',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT 'Cover Image URL',
  `start_time` DATETIME NOT NULL COMMENT 'Start Time',
  `end_time` DATETIME NOT NULL COMMENT 'End Time',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'Status: 0-NotStarted 1-Active 2-Ended',
  `group_count` INT DEFAULT 1 COMMENT 'Group Count',
  `sms_weight` INT DEFAULT 100 COMMENT 'SMS Vote Weight',
  `ivr_weight` INT DEFAULT 100 COMMENT 'IVR Vote Weight',
  `web_weight` INT DEFAULT 100 COMMENT 'Web Vote Weight',
  `app_weight` INT DEFAULT 100 COMMENT 'App Vote Weight',
  `wechat_weight` INT DEFAULT 100 COMMENT 'WeChat Vote Weight',
  `pay_weight` INT DEFAULT 100 COMMENT 'Pay Vote Weight',
  `ip_limit` INT DEFAULT 0 COMMENT 'IP Limit per day, 0=unlimited',
  `enable_captcha` TINYINT DEFAULT 1 COMMENT 'Enable Captcha: 0-No 1-Yes',
  `show_votes` TINYINT DEFAULT 1 COMMENT 'Show Votes: 0-No 1-Yes',
  `domain` VARCHAR(100) DEFAULT NULL COMMENT 'Domain',
  `template` VARCHAR(100) DEFAULT 'default' COMMENT 'Template',
  `rules` TEXT COMMENT 'Rules',
  `create_by` VARCHAR(64) DEFAULT NULL COMMENT 'Creator',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_by` VARCHAR(64) DEFAULT NULL COMMENT 'Updater',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `deleted` TINYINT DEFAULT 0 COMMENT 'Delete Flag: 0-Normal 1-Deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_service_id` (`service_id`),
  KEY `idx_region` (`region`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Vote Activity Table';

-- ================================
-- 2. Vote Candidate Table
-- ================================
DROP TABLE IF EXISTS `vote_candidate`;
CREATE TABLE `vote_candidate` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `activity_id` BIGINT UNSIGNED NOT NULL COMMENT 'Activity ID',
  `service_id` VARCHAR(64) NOT NULL COMMENT 'Activity Service ID',
  `candidate_no` VARCHAR(20) NOT NULL COMMENT 'Candidate Number',
  `name` VARCHAR(100) NOT NULL COMMENT 'Candidate Name',
  `group_name` VARCHAR(50) DEFAULT '1' COMMENT 'Group Name',
  `description` TEXT COMMENT 'Description',
  `photo` VARCHAR(500) DEFAULT NULL COMMENT 'Photo URL',
  `sms_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'SMS Votes',
  `ivr_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'IVR Votes',
  `web_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'Web Votes',
  `app_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'App Votes',
  `wechat_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'WeChat Votes',
  `pay_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'Pay Votes',
  `total_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'Total Votes',
  `score` DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Score',
  `ranking` INT DEFAULT 0 COMMENT 'Ranking',
  `status` TINYINT DEFAULT 0 COMMENT 'Status: 0-Normal 1-Disabled',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `deleted` TINYINT DEFAULT 0 COMMENT 'Delete Flag: 0-Normal 1-Deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_service_candidate` (`service_id`, `candidate_no`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_group_name` (`group_name`),
  KEY `idx_total_votes` (`total_votes`),
  KEY `idx_score` (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Vote Candidate Table';

-- ================================
-- 3. Vote Record Table
-- ================================
DROP TABLE IF EXISTS `vote_record`;
CREATE TABLE `vote_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `activity_id` BIGINT UNSIGNED NOT NULL COMMENT 'Activity ID',
  `candidate_id` BIGINT UNSIGNED NOT NULL COMMENT 'Candidate ID',
  `channel` VARCHAR(20) NOT NULL COMMENT 'Channel: SMS/IVR/WEB/APP/WECHAT/PAY',
  `voter_phone` VARCHAR(20) DEFAULT NULL COMMENT 'Voter Phone',
  `voter_ip` VARCHAR(50) DEFAULT NULL COMMENT 'Voter IP',
  `voter_id` VARCHAR(100) DEFAULT NULL COMMENT 'Voter ID',
  `vote_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Vote Time',
  `status` TINYINT DEFAULT 1 COMMENT 'Status: 0-Invalid 1-Valid',
  PRIMARY KEY (`id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_candidate` (`candidate_id`),
  KEY `idx_channel` (`channel`),
  KEY `idx_vote_time` (`vote_time`),
  KEY `idx_voter_phone` (`voter_phone`),
  KEY `idx_voter_ip` (`voter_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Vote Record Table';

-- ================================
-- 4. Vote Limit Table
-- ================================
DROP TABLE IF EXISTS `vote_limit`;
CREATE TABLE `vote_limit` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `activity_id` BIGINT UNSIGNED NOT NULL COMMENT 'Activity ID',
  `limit_type` VARCHAR(20) NOT NULL COMMENT 'Limit Type: IP/PHONE/IDCARD',
  `limit_key` VARCHAR(100) NOT NULL COMMENT 'Limit Key',
  `vote_count` INT DEFAULT 0 COMMENT 'Vote Count',
  `last_vote_time` DATETIME DEFAULT NULL COMMENT 'Last Vote Time',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_type_key` (`activity_id`, `limit_type`, `limit_key`),
  KEY `idx_activity_type` (`activity_id`, `limit_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Vote Limit Table';

-- ================================
-- 5. System User Table
-- ================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `username` VARCHAR(50) NOT NULL COMMENT 'Username',
  `password` VARCHAR(200) NOT NULL COMMENT 'Password (Encrypted)',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT 'Nickname',
  `email` VARCHAR(100) DEFAULT NULL COMMENT 'Email',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT 'Avatar',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT 'Role: ADMIN/USER',
  `status` TINYINT DEFAULT 1 COMMENT 'Status: 0-Disabled 1-Enabled',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `deleted` TINYINT DEFAULT 0 COMMENT 'Delete Flag: 0-Normal 1-Deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System User Table';

-- Insert default admin user
-- Password: admin123 (BCrypt encrypted)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `role`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Administrator', 'ADMIN');

-- ================================
-- 6. Vote Statistics Table
-- ================================
DROP TABLE IF EXISTS `vote_statistics`;
CREATE TABLE `vote_statistics` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary Key',
  `activity_id` BIGINT UNSIGNED NOT NULL COMMENT 'Activity ID',
  `stat_date` DATE NOT NULL COMMENT 'Statistics Date',
  `total_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'Total Votes',
  `sms_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'SMS Votes',
  `ivr_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'IVR Votes',
  `web_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'Web Votes',
  `app_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'App Votes',
  `wechat_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'WeChat Votes',
  `pay_votes` BIGINT UNSIGNED DEFAULT 0 COMMENT 'Pay Votes',
  `unique_voters` INT DEFAULT 0 COMMENT 'Unique Voters',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_date` (`activity_id`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Vote Statistics Table';

-- ================================
-- Insert Sample Data
-- ================================

-- Sample Activity
INSERT INTO `vote_activity` 
(`service_id`, `title`, `subtitle`, `region`, `description`, `start_time`, `end_time`, `status`) 
VALUES 
('demo-2025-001', 'DZVOTE 2.0 Demo Activity', 'System Testing', 'Shandong', 'This is a demo voting activity for testing the new system.', '2025-01-01 00:00:00', '2025-12-31 23:59:59', 1);

-- Sample Candidates
INSERT INTO `vote_candidate` 
(`activity_id`, `service_id`, `candidate_no`, `name`, `group_name`, `description`, `web_votes`, `total_votes`) 
VALUES 
(1, 'demo-2025-001', '001', 'Candidate A', '1', 'Excellent performance in the field', 1250, 1250),
(1, 'demo-2025-001', '002', 'Candidate B', '1', 'Outstanding contributions', 980, 980),
(1, 'demo-2025-001', '003', 'Candidate C', '1', 'Innovation leader', 1580, 1580),
(1, 'demo-2025-001', '004', 'Candidate D', '1', 'Community service expert', 750, 750),
(1, 'demo-2025-001', '005', 'Candidate E', '1', 'Youth role model', 1120, 1120);

SET FOREIGN_KEY_CHECKS = 1;

-- ================================
-- Verification
-- ================================
SELECT 'Database initialization completed successfully!' AS Status;
SELECT COUNT(*) AS activity_count FROM vote_activity;
SELECT COUNT(*) AS candidate_count FROM vote_candidate;
SELECT COUNT(*) AS user_count FROM sys_user;
