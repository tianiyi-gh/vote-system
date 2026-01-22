package com.dzvote.vote.mapper;

import com.dzvote.vote.entity.VerificationCode;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 验证码Mapper
 */
@Mapper
public interface VerificationCodeMapper {

    /**
     * 创建验证码记录
     */
    @Insert("INSERT INTO verification_codes (type, target, code, purpose, verified, used, expire_time, create_time, ip_address, device_fingerprint) " +
            "VALUES (#{type}, #{target}, #{code}, #{purpose}, 0, 0, #{expireTime}, #{createTime}, #{ipAddress}, #{deviceFingerprint})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VerificationCode verificationCode);

    /**
     * 根据ID查找验证码
     */
    @Select("SELECT * FROM verification_codes WHERE id = #{id}")
    VerificationCode findById(Long id);

    /**
     * 根据目标地址和用途查找最新的未验证验证码
     */
    @Select("SELECT * FROM verification_codes " +
            "WHERE target = #{target} AND purpose = #{purpose} AND verified = 0 " +
            "AND expire_time > NOW() " +
            "ORDER BY create_time DESC LIMIT 1")
    VerificationCode findLatestUnverified(@Param("target") String target, @Param("purpose") String purpose);

    /**
     * 标记验证码为已验证
     */
    @Update("UPDATE verification_codes SET verified = 1, verify_time = #{verifyTime} WHERE id = #{id}")
    int markAsVerified(@Param("id") Long id, @Param("verifyTime") LocalDateTime verifyTime);

    /**
     * 标记验证码为已使用
     */
    @Update("UPDATE verification_codes SET used = 1 WHERE id = #{id}")
    int markAsUsed(Long id);

    /**
     * 获取某IP/设备在指定时间内的验证码发送次数
     */
    @Select("SELECT COUNT(*) FROM verification_codes " +
            "WHERE (ip_address = #{ipAddress} OR device_fingerprint = #{deviceFingerprint}) " +
            "AND create_time > #{startTime}")
    int countRecentCodes(@Param("ipAddress") String ipAddress, 
                       @Param("deviceFingerprint") String deviceFingerprint,
                       @Param("startTime") LocalDateTime startTime);

    /**
     * 删除过期验证码
     */
    @Delete("DELETE FROM verification_codes WHERE expire_time < NOW()")
    int deleteExpired();

    /**
     * 获取验证码发送统计（按IP分组）
     */
    @Select("SELECT ip_address, COUNT(*) as send_count " +
            "FROM verification_codes " +
            "WHERE create_time > #{startTime} " +
            "GROUP BY ip_address " +
            "ORDER BY send_count DESC " +
            "LIMIT #{limit}")
    List<VerificationCodeStats> getSendStatsByIp(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

    /**
     * 验证码统计结果
     */
    class VerificationCodeStats {
        private String ipAddress;
        private Integer sendCount;

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public Integer getSendCount() { return sendCount; }
        public void setSendCount(Integer sendCount) { this.sendCount = sendCount; }
    }
}
