package com.dzvote.vote.mapper;

import com.dzvote.vote.entity.VoteLimit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 投票限制Mapper
 */
@Mapper
public interface VoteLimitMapper {

    /**
     * 查询今日投票次数
     */
    @Select("SELECT vote_count FROM vote_limits WHERE activity_id = #{activityId} " +
            "AND voter_phone = #{voterPhone} " +
            "AND DATE(last_vote_time) = CURDATE()")
    Integer getTodayVoteCount(@Param("activityId") Long activityId,
                              @Param("voterPhone") String voterPhone);

    /**
     * 增加投票次数
     */
    @Update("INSERT INTO vote_limits (activity_id, voter_phone, voter_ip, vote_count, last_vote_time, created_at) " +
            "VALUES (#{activityId}, #{voterPhone}, #{voterIp}, 1, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE vote_count = vote_count + 1, last_vote_time = NOW()")
    void incrementVoteCount(@Param("activityId") Long activityId,
                           @Param("voterPhone") String voterPhone,
                           @Param("voterIp") String voterIp);
}