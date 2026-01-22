-- Security tables for anti-fraud voting

-- Verification codes table
CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    type VARCHAR(10) NOT NULL COMMENT 'Type: SMS/EMAIL',
    target VARCHAR(100) NOT NULL COMMENT 'Target: Phone/Email',
    code VARCHAR(10) NOT NULL COMMENT 'Verification Code',
    purpose VARCHAR(20) NOT NULL COMMENT 'Purpose: VOTE/LOGIN/REGISTER',
    verified TINYINT DEFAULT 0 COMMENT 'Verified: 0-No 1-Yes',
    used TINYINT DEFAULT 0 COMMENT 'Used: 0-No 1-Yes',
    expire_time DATETIME NOT NULL COMMENT 'Expire Time',
    create_time DATETIME NOT NULL COMMENT 'Create Time',
    verify_time DATETIME COMMENT 'Verify Time',
    ip_address VARCHAR(50) COMMENT 'IP Address',
    device_fingerprint VARCHAR(50) COMMENT 'Device Fingerprint',
    INDEX idx_target (target),
    INDEX idx_create_time (create_time),
    INDEX idx_expire_time (expire_time),
    INDEX idx_ip_device (ip_address, device_fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Verification Codes';

-- IP blacklist table
CREATE TABLE IF NOT EXISTS ip_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    ip_address VARCHAR(50) NOT NULL COMMENT 'IP Address',
    reason VARCHAR(200) COMMENT 'Block Reason',
    type VARCHAR(20) NOT NULL DEFAULT 'TEMPORARY' COMMENT 'Type: PERMANENT/TEMPORARY',
    start_time DATETIME NOT NULL COMMENT 'Start Time',
    end_time DATETIME COMMENT 'End Time',
    status TINYINT DEFAULT 1 COMMENT 'Status: 0-Released 1-Active',
    operator VARCHAR(50) COMMENT 'Operator',
    create_time DATETIME NOT NULL COMMENT 'Create Time',
    remark VARCHAR(500) COMMENT 'Remark',
    UNIQUE KEY uk_ip (ip_address, start_time),
    INDEX idx_status (status),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP Blacklist';

-- Add device fingerprint and location to vote_records table
ALTER TABLE vote_records
ADD COLUMN IF NOT EXISTS device_fingerprint VARCHAR(50) COMMENT 'Device Fingerprint',
ADD COLUMN IF NOT EXISTS location VARCHAR(200) COMMENT 'Location',
ADD INDEX idx_device_fingerprint (device_fingerprint);
