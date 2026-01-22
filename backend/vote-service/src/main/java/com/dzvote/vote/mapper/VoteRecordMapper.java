package com.dzvote.vote.mapper;

import com.dzvote.vote.entity.VoteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 投票记录Mapper
 */
@Mapper
public interface VoteRecordMapper {

    /**
     * 插入投票记录
     */
    @Insert("INSERT INTO vote_records (activity_id, candidate_id, channel, voter_phone, voter_ip, valid, vote_time) " +
            "VALUES (#{activityId}, #{candidateId}, #{channel}, #{voterPhone}, #{voterIp}, #{valid}, #{voteTime})")
    int insert(VoteRecord record);

    /**
     * 统计候选人各渠道票数
     */
    @Select("SELECT " +
            "SUM(CASE WHEN channel = 'SMS' THEN 1 ELSE 0 END) as smsVotes, " +
            "SUM(CASE WHEN channel = 'IVR' THEN 1 ELSE 0 END) as ivrVotes, " +
            "SUM(CASE WHEN channel = 'WEB' THEN 1 ELSE 0 END) as webVotes, " +
            "SUM(CASE WHEN channel = 'APP' THEN 1 ELSE 0 END) as appVotes, " +
            "SUM(CASE WHEN channel = 'WECHAT' THEN 1 ELSE 0 END) as wechatVotes, " +
            "SUM(CASE WHEN channel = 'PAY' THEN 1 ELSE 0 END) as payVotes, " +
            "COUNT(*) as totalVotes " +
            "FROM vote_records WHERE candidate_id = #{candidateId} AND valid = 1")
    VoteRecordStats countCandidateVotes(@Param("candidateId") Long candidateId);

    /**
     * 统计活动每日投票数据
     */
    @Select("SELECT " +
            "COUNT(*) as totalVotes, " +
            "SUM(CASE WHEN channel = 'SMS' THEN 1 ELSE 0 END) as smsVotes, " +
            "SUM(CASE WHEN channel = 'IVR' THEN 1 ELSE 0 END) as ivrVotes, " +
            "SUM(CASE WHEN channel = 'WEB' THEN 1 ELSE 0 END) as webVotes, " +
            "SUM(CASE WHEN channel = 'APP' THEN 1 ELSE 0 END) as appVotes, " +
            "SUM(CASE WHEN channel = 'WECHAT' THEN 1 ELSE 0 END) as wechatVotes, " +
            "SUM(CASE WHEN channel = 'PAY' THEN 1 ELSE 0 END) as payVotes " +
            "FROM vote_records WHERE activity_id = #{activityId} AND DATE(vote_time) = #{date} AND valid = 1")
    VoteRecordStats countActivityDailyVotes(@Param("activityId") Long activityId, @Param("date") LocalDate date);

    /**
     * 统计活动独立投票人数
     */
    @Select("SELECT COUNT(DISTINCT voter_phone) FROM vote_records " +
            "WHERE activity_id = #{activityId} AND DATE(vote_time) = #{date} AND valid = 1")
    Integer countActivityUniqueVoters(@Param("activityId") Long activityId, @Param("date") LocalDate date);

    /**
     * 增加候选人票数（同时更新votes和total_votes字段）
     */
    @Update("UPDATE candidates SET votes = votes + 1, total_votes = total_votes + 1 WHERE id = #{candidateId}")
    void incrementCandidateVotes(@Param("candidateId") Long candidateId);

    /**
     * 根据渠道增加候选人票数
     */
    @Update("<script>" +
            "UPDATE candidates SET " +
            "<choose>" +
            "<when test='column == \"sms_votes\"'>sms_votes = sms_votes + 1</when>" +
            "<when test='column == \"ivr_votes\"'>ivr_votes = ivr_votes + 1</when>" +
            "<when test='column == \"web_votes\"'>web_votes = web_votes + 1</when>" +
            "<when test='column == \"app_votes\"'>app_votes = app_votes + 1</when>" +
            "<when test='column == \"wechat_votes\"'>wechat_votes = wechat_votes + 1</when>" +
            "<when test='column == \"pay_votes\"'>pay_votes = pay_votes + 1</when>" +
            "</choose>" +
            " WHERE id = #{candidateId}" +
            "</script>")
    void incrementCandidateVotesByChannel(@Param("candidateId") Long candidateId,
                                         @Param("column") String column);

    /**
     * 查询投票记录（分页，带候选人名称）
     */
    @Select("SELECT " +
            "vr.id as id, " +
            "vr.activity_id as activityId, " +
            "vr.candidate_id as candidateId, " +
            "vr.voter_name as voterName, " +
            "vr.voter_phone as voterPhone, " +
            "vr.voter_ip as voterIp, " +
            "vr.channel as channel, " +
            "vr.vote_time as voteTime, " +
            "vr.valid as valid, " +
            "vr.device_info as deviceInfo, " +
            "vr.location as location, " +
            "c.name as candidateName, " +
            "c.avatar as candidateAvatar, " +
            "c.order_num as candidateNo " +
            "FROM vote_records vr " +
            "LEFT JOIN candidates c ON vr.candidate_id = c.id " +
            "WHERE vr.activity_id = #{activityId} " +
            "ORDER BY vr.vote_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<VoteRecord> findVoteRecords(@Param("activityId") Long activityId,
                                       @Param("offset") Integer offset,
                                       @Param("limit") Integer limit);

    /**
     * 获取活动投票趋势（最近N天）
     */
    @Select("SELECT " +
            "DATE(vote_time) as date, " +
            "COUNT(*) as votes " +
            "FROM vote_records " +
            "WHERE activity_id = #{activityId} AND valid = 1 " +
            "AND DATE(vote_time) >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(vote_time) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getActivityVoteTrends(@Param("activityId") Long activityId, @Param("days") Integer days);

    /**
     * 根据时间范围获取活动投票趋势
     */
    @Select("SELECT " +
            "DATE(vote_time) as voteDate, " +
            "COUNT(*) as totalVotes, " +
            "COUNT(DISTINCT voter_ip) as uniqueVoters, " +
            "SUM(CASE WHEN channel = 'SMS' THEN 1 ELSE 0 END) as smsVotes, " +
            "SUM(CASE WHEN channel = 'WEB' THEN 1 ELSE 0 END) as webVotes, " +
            "SUM(CASE WHEN channel = 'WECHAT' THEN 1 ELSE 0 END) as wechatVotes " +
            "FROM vote_records " +
            "WHERE activity_id = #{activityId} AND valid = 1 " +
            "AND DATE(vote_time) BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(vote_time) " +
            "ORDER BY voteDate")
    List<Map<String, Object>> getActivityVoteTrendsByRange(
            @Param("activityId") Long activityId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 获取候选人得票率统计
     */
    @Select("SELECT " +
            "c.id, " +
            "c.name, " +
            "c.votes, " +
            "c.activity_id as activity_id, " +
            "c.status, " +
            "c.votes * 100.0 / (SELECT SUM(votes) FROM candidates WHERE activity_id = #{activityId}) as voteRate " +
            "FROM candidates c " +
            "WHERE c.activity_id = #{activityId} " +
            "AND c.status = 1 " +
            "ORDER BY c.votes DESC")
    List<Map<String, Object>> getCandidateVoteRates(@Param("activityId") Long activityId);

    /**
     * 获取活动参与度分析（每小时）
     */
    @Select("SELECT " +
            "HOUR(vote_time) as hour, " +
            "COUNT(*) as voteCount, " +
            "COUNT(DISTINCT voter_ip) as uniqueVoters " +
            "FROM vote_records " +
            "WHERE activity_id = #{activityId} AND valid = 1 " +
            "AND DATE(vote_time) = #{date} " +
            "GROUP BY HOUR(vote_time) " +
            "ORDER BY hour ASC")
    List<Map<String, Object>> getHourlyParticipation(@Param("activityId") Long activityId, @Param("date") LocalDate date);

    /**
     * 查询投票记录用于导出
     */
    @Select("<script>" +
            "SELECT " +
            "id, activity_id, candidate_id, voter_phone, voter_ip, " +
            "vote_time, channel, valid " +
            "FROM vote_records " +
            "WHERE activity_id = #{activityId} AND valid = 1 " +
            "<if test='startDate != null and startDate != \"\"'>" +
            "AND DATE(vote_time) &gt;= #{startDate} " +
            "</if>" +
            "<if test='endDate != null and endDate != \"\"'>" +
            "AND DATE(vote_time) &lt;= #{endDate} " +
            "</if>" +
            "ORDER BY vote_time DESC " +
            "</script>")
    List<Map<String, Object>> findVoteRecordsByActivity(@Param("activityId") Long activityId,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate);

    /**
     * 统计IP在指定时间后的投票数
     */
    @Select("SELECT COUNT(*) FROM vote_records " +
            "WHERE voter_ip = #{ipAddress} AND vote_time > #{startTime} AND valid = 1")
    int countVotesByIpAndTime(@Param("ipAddress") String ipAddress, @Param("startTime") java.time.LocalDateTime startTime);

    /**
     * 统计设备在指定时间后的投票数
     */
    @Select("SELECT COUNT(*) FROM vote_records " +
            "WHERE device_info = #{deviceFingerprint} AND vote_time > #{startTime} AND valid = 1")
    int countVotesByDeviceAndTime(@Param("deviceFingerprint") String deviceFingerprint, @Param("startTime") java.time.LocalDateTime startTime);

    /**
     * 查询IP在指定时间后的投票记录
     */
    @Select("SELECT candidate_id FROM vote_records " +
            "WHERE voter_ip = #{ipAddress} AND vote_time > #{startTime} AND valid = 1 " +
            "ORDER BY vote_time DESC LIMIT 100")
    List<java.util.Map<String, Object>> findRecentVotesByIp(@Param("ipAddress") String ipAddress, @Param("startTime") java.time.LocalDateTime startTime);

    /**
     * 统计用户今天对多少个不同候选人投过票
     */
    @Select("SELECT COUNT(DISTINCT candidate_id) FROM vote_records " +
            "WHERE activity_id = #{activityId} " +
            "AND (voter_phone = #{voterPhone} OR voter_ip = #{voterIp}) " +
            "AND DATE(vote_time) = CURDATE() " +
            "AND valid = 1")
    Integer countTodayVotedCandidates(@Param("activityId") Long activityId,
                                      @Param("voterPhone") String voterPhone,
                                      @Param("voterIp") String voterIp);

    /**
     * 统计用户今天对某个候选人投了多少票
     */
    @Select("SELECT COUNT(*) FROM vote_records " +
            "WHERE activity_id = #{activityId} " +
            "AND candidate_id = #{candidateId} " +
            "AND (voter_phone = #{voterPhone} OR voter_ip = #{voterIp}) " +
            "AND DATE(vote_time) = CURDATE() " +
            "AND valid = 1")
    Integer countTodayVotesForCandidate(@Param("activityId") Long activityId,
                                         @Param("candidateId") Long candidateId,
                                         @Param("voterPhone") String voterPhone,
                                         @Param("voterIp") String voterIp);

    /**
     * 根据手机号查询用户投票记录
     */
    @Select("SELECT " +
            "vr.id as id, " +
            "vr.activity_id as activityId, " +
            "vr.candidate_id as candidateId, " +
            "vr.voter_name as voterName, " +
            "vr.voter_phone as voterPhone, " +
            "vr.voter_ip as voterIp, " +
            "vr.channel as channel, " +
            "vr.vote_time as voteTime, " +
            "vr.valid as valid, " +
            "vr.device_info as deviceInfo, " +
            "vr.location as location, " +
            "c.name as candidateName, " +
            "c.avatar as candidateAvatar, " +
            "c.order_num as candidateNo " +
            "FROM vote_records vr " +
            "LEFT JOIN candidates c ON vr.candidate_id = c.id " +
            "WHERE vr.activity_id = #{activityId} " +
            "AND vr.voter_phone = #{voterPhone} " +
            "AND vr.valid = 1 " +
            "ORDER BY vr.vote_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<VoteRecord> findVoteRecordsByPhone(@Param("activityId") Long activityId,
                                             @Param("voterPhone") String voterPhone,
                                             @Param("offset") Integer offset,
                                             @Param("limit") Integer limit);

    /**
     * 根据IP查询用户投票记录
     */
    @Select("SELECT " +
            "vr.id as id, " +
            "vr.activity_id as activityId, " +
            "vr.candidate_id as candidateId, " +
            "vr.voter_name as voterName, " +
            "vr.voter_phone as voterPhone, " +
            "vr.voter_ip as voterIp, " +
            "vr.channel as channel, " +
            "vr.vote_time as voteTime, " +
            "vr.valid as valid, " +
            "vr.device_info as deviceInfo, " +
            "vr.location as location, " +
            "c.name as candidateName, " +
            "c.avatar as candidateAvatar, " +
            "c.order_num as candidateNo " +
            "FROM vote_records vr " +
            "LEFT JOIN candidates c ON vr.candidate_id = c.id " +
            "WHERE vr.activity_id = #{activityId} " +
            "AND vr.voter_ip = #{voterIp} " +
            "AND vr.valid = 1 " +
            "ORDER BY vr.vote_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<VoteRecord> findVoteRecordsByIp(@Param("activityId") Long activityId,
                                          @Param("voterIp") String voterIp,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);

    /**
     * 根据手机号统计用户投票数据
     */
    @Select("SELECT " +
            "COUNT(*) as totalVotes, " +
            "COUNT(DISTINCT candidate_id) as votedCandidates, " +
            "SUM(CASE WHEN DATE(vote_time) = CURDATE() THEN 1 ELSE 0 END) as todayVotes, " +
            "MAX(vote_time) as lastVoteTime " +
            "FROM vote_records " +
            "WHERE activity_id = #{activityId} " +
            "AND voter_phone = #{voterPhone} " +
            "AND valid = 1")
    Map<String, Object> countUserVoteStatsByPhone(@Param("activityId") Long activityId,
                                                     @Param("voterPhone") String voterPhone);

    /**
     * 根据IP统计用户投票数据
     */
    @Select("SELECT " +
            "COUNT(*) as totalVotes, " +
            "COUNT(DISTINCT candidate_id) as votedCandidates, " +
            "SUM(CASE WHEN DATE(vote_time) = CURDATE() THEN 1 ELSE 0 END) as todayVotes, " +
            "MAX(vote_time) as lastVoteTime " +
            "FROM vote_records " +
            "WHERE activity_id = #{activityId} " +
            "AND voter_ip = #{voterIp} " +
            "AND valid = 1")
    Map<String, Object> countUserVoteStatsByIp(@Param("activityId") Long activityId,
                                                 @Param("voterIp") String voterIp);

    /**
     * 获取候选人每日投票排名
     */
    @Select("SELECT " +
            "c.id, " +
            "c.name, " +
            "c.avatar, " +
            "c.order_num as candidateNo, " +
            "COUNT(vr.id) as dailyVotes, " +
            "c.total_votes as totalVotes, " +
            "ROW_NUMBER() OVER (ORDER BY COUNT(vr.id) DESC) as ranking " +
            "FROM candidates c " +
            "LEFT JOIN vote_records vr ON c.id = vr.candidate_id " +
            "AND vr.activity_id = #{activityId} " +
            "AND DATE(vr.vote_time) = #{date} " +
            "AND vr.valid = 1 " +
            "WHERE c.activity_id = #{activityId} " +
            "AND c.status = 1 " +
            "GROUP BY c.id, c.name, c.avatar, c.order_num, c.total_votes " +
            "ORDER BY dailyVotes DESC")
    List<Map<String, Object>> getCandidateDailyRanking(@Param("activityId") Long activityId,
                                                         @Param("date") LocalDate date);

    /**
     * 投票记录统计内部类
     */
    class VoteRecordStats {
        private Long totalVotes;
        private Long smsVotes;
        private Long ivrVotes;
        private Long webVotes;
        private Long appVotes;
        private Long wechatVotes;
        private Long payVotes;

        // Getters and Setters
        public Long getTotalVotes() { return totalVotes; }
        public void setTotalVotes(Long totalVotes) { this.totalVotes = totalVotes; }
        public Long getSmsVotes() { return smsVotes; }
        public void setSmsVotes(Long smsVotes) { this.smsVotes = smsVotes; }
        public Long getIvrVotes() { return ivrVotes; }
        public void setIvrVotes(Long ivrVotes) { this.ivrVotes = ivrVotes; }
        public Long getWebVotes() { return webVotes; }
        public void setWebVotes(Long webVotes) { this.webVotes = webVotes; }
        public Long getAppVotes() { return appVotes; }
        public void setAppVotes(Long appVotes) { this.appVotes = appVotes; }
        public Long getWechatVotes() { return wechatVotes; }
        public void setWechatVotes(Long wechatVotes) { this.wechatVotes = wechatVotes; }
        public Long getPayVotes() { return payVotes; }
        public void setPayVotes(Long payVotes) { this.payVotes = payVotes; }
    }
}