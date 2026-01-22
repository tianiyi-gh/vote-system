package com.dzvote.vote.mapper;

import com.dzvote.vote.entity.IpBlacklist;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IP黑名单Mapper
 */
@Mapper
public interface IpBlacklistMapper {

    /**
     * 添加IP到黑名单
     */
    @Insert("INSERT INTO ip_blacklist (ip_address, reason, type, start_time, end_time, status, operator, create_time, remark) " +
            "VALUES (#{ipAddress}, #{reason}, #{type}, #{startTime}, #{endTime}, 1, #{operator}, #{createTime}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(IpBlacklist ipBlacklist);

    /**
     * 根据ID查找
     */
    @Select("SELECT * FROM ip_blacklist WHERE id = #{id}")
    IpBlacklist findById(Long id);

    /**
     * 根据IP地址查找生效中的封禁记录
     */
    @Select("SELECT * FROM ip_blacklist " +
            "WHERE ip_address = #{ipAddress} AND status = 1 " +
            "AND (type = 'PERMANENT' OR end_time > NOW()) " +
            "ORDER BY create_time DESC LIMIT 1")
    IpBlacklist findByIpAddress(String ipAddress);

    /**
     * 获取所有黑名单记录
     */
    @Select("SELECT * FROM ip_blacklist ORDER BY create_time DESC")
    List<IpBlacklist> findAll();

    /**
     * 获取生效中的黑名单
     */
    @Select("SELECT * FROM ip_blacklist " +
            "WHERE status = 1 " +
            "AND (type = 'PERMANENT' OR end_time > NOW()) " +
            "ORDER BY create_time DESC")
    List<IpBlacklist> findActive();

    /**
     * 解封IP
     */
    @Update("UPDATE ip_blacklist SET status = 0 WHERE id = #{id}")
    int unblock(Long id);

    /**
     * 删除黑名单记录
     */
    @Delete("DELETE FROM ip_blacklist WHERE id = #{id}")
    int delete(Long id);

    /**
     * 检查IP是否在黑名单中
     */
    @Select("SELECT COUNT(*) FROM ip_blacklist " +
            "WHERE ip_address = #{ipAddress} AND status = 1 " +
            "AND (type = 'PERMANENT' OR end_time > NOW())")
    int isBlocked(String ipAddress);

    /**
     * 批量封禁IP
     */
    @Insert("<script>" +
            "INSERT INTO ip_blacklist (ip_address, reason, type, start_time, end_time, status, operator, create_time, remark) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.ipAddress}, #{item.reason}, #{item.type}, #{item.startTime}, #{item.endTime}, 1, #{item.operator}, #{item.createTime}, #{item.remark})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<IpBlacklist> list);
}
