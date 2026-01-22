-- 防刷票安全相关表

-- 验证码表
CREATE TABLE IF NOT EXISTS verification_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    type VARCHAR(10) NOT NULL COMMENT '接收方式：SMS/EMAIL',
    target VARCHAR(100) NOT NULL COMMENT '接收地址：手机号/邮箱',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    purpose VARCHAR(20) NOT NULL COMMENT '验证码用途：VOTE/LOGIN/REGISTER',
    verified TINYINT DEFAULT 0 COMMENT '是否已验证：0-未验证 1-已验证',
    used TINYINT DEFAULT 0 COMMENT '是否已使用：0-未使用 1-已使用',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    verify_time DATETIME COMMENT '验证时间',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    device_fingerprint VARCHAR(50) COMMENT '设备指纹',
    INDEX idx_target (target),
    INDEX idx_create_time (create_time),
    INDEX idx_expire_time (expire_time),
    INDEX idx_ip_device (ip_address, device_fingerprint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';

-- IP黑名单表
CREATE TABLE IF NOT EXISTS ip_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    ip_address VARCHAR(50) NOT NULL COMMENT 'IP地址',
    reason VARCHAR(200) COMMENT '封禁原因',
    type VARCHAR(20) NOT NULL DEFAULT 'TEMPORARY' COMMENT '封禁类型：PERMANENT/TEMPORARY',
    start_time DATETIME NOT NULL COMMENT '封禁开始时间',
    end_time DATETIME COMMENT '封禁结束时间',
    status TINYINT DEFAULT 1 COMMENT '封禁状态：0-已解除 1-生效中',
    operator VARCHAR(50) COMMENT '操作员',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    remark VARCHAR(500) COMMENT '备注',
    UNIQUE KEY uk_ip (ip_address, start_time),
    INDEX idx_status (status),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IP黑名单表';

-- 为vote_records表添加设备指纹和位置字段（如果不存在）
ALTER TABLE vote_records
ADD COLUMN IF NOT EXISTS device_fingerprint VARCHAR(50) COMMENT '设备指纹',
ADD COLUMN IF NOT EXISTS location VARCHAR(200) COMMENT '位置信息',
ADD INDEX idx_device_fingerprint (device_fingerprint);
