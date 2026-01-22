package com.dzvote.vote.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 验证码实体
 */
@Data
public class VerificationCode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 接收方式：SMS/EMAIL
     */
    private String type;

    /**
     * 接收地址：手机号/邮箱
     */
    private String target;

    /**
     * 验证码
     */
    private String code;

    /**
     * 验证码用途：VOTE/LOGIN/REGISTER
     */
    private String purpose;

    /**
     * 是否已验证：0-未验证 1-已验证
     */
    private Integer verified;

    /**
     * 是否已使用：0-未使用 1-已使用
     */
    private Integer used;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 验证时间
     */
    private LocalDateTime verifyTime;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 设备指纹
     */
    private String deviceFingerprint;
}
