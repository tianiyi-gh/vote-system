package com.dzvote.vote.mapper;

import com.dzvote.vote.entity.Activity;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 活动Mapper
 */
@Mapper
public interface ActivityMapper {

    /**
     * 查询所有活动
     */
    @Select("SELECT id, title, subtitle, region, description, cover_image AS coverImage, " +
            "start_time AS startTime, end_time AS endTime, status, vote_limit AS voteLimit, " +
            "group_count AS groupCount, sms_weight AS smsWeight, ivr_weight AS ivrWeight, " +
            "web_weight AS webWeight, app_weight AS appWeight, wechat_weight AS wechatWeight, " +
            "pay_weight AS payWeight, ip_limit AS ipLimit, enable_captcha AS enableCaptcha, " +
            "show_votes AS showVotes, domain, template, rules, service_id AS serviceId, " +
            "candidate_count AS candidateCount, total_votes AS totalVotes, " +
            "daily_candidate_limit AS dailyCandidateLimit, " +
            "candidate_daily_limit AS candidateDailyLimit, " +
            "created_by AS createBy, created_at AS createTime, updated_at AS updateTime, " +
            "update_by AS updateBy, deleted " +
            "FROM activities WHERE deleted = 0 ORDER BY id DESC")
    List<Activity> findAll();

    /**
     * 根据ID查询活动
     */
    @Select("SELECT id, title, subtitle, region, description, cover_image AS coverImage, " +
            "start_time AS startTime, end_time AS endTime, status, vote_limit AS voteLimit, " +
            "group_count AS groupCount, sms_weight AS smsWeight, ivr_weight AS ivrWeight, " +
            "web_weight AS webWeight, app_weight AS appWeight, wechat_weight AS wechatWeight, " +
            "pay_weight AS payWeight, ip_limit AS ipLimit, enable_captcha AS enableCaptcha, " +
            "show_votes AS showVotes, domain, template, rules, service_id AS serviceId, " +
            "candidate_count AS candidateCount, total_votes AS totalVotes, " +
            "daily_candidate_limit AS dailyCandidateLimit, " +
            "candidate_daily_limit AS candidateDailyLimit, " +
            "created_by AS createBy, created_at AS createTime, updated_at AS updateTime, " +
            "update_by AS updateBy, deleted " +
            "FROM activities WHERE id = #{id} AND deleted = 0")
    Activity findById(@Param("id") Long id);

    /**
     * 根据状态查询活动
     */
    @Select("SELECT id, title, subtitle, region, description, cover_image AS coverImage, " +
            "start_time AS startTime, end_time AS endTime, status, vote_limit AS voteLimit, " +
            "group_count AS groupCount, sms_weight AS smsWeight, ivr_weight AS ivrWeight, " +
            "web_weight AS webWeight, app_weight AS appWeight, wechat_weight AS wechatWeight, " +
            "pay_weight AS payWeight, ip_limit AS ipLimit, enable_captcha AS enableCaptcha, " +
            "show_votes AS showVotes, domain, template, rules, service_id AS serviceId, " +
            "candidate_count AS candidateCount, total_votes AS totalVotes, " +
            "daily_candidate_limit AS dailyCandidateLimit, " +
            "candidate_daily_limit AS candidateDailyLimit, " +
            "created_by AS createBy, created_at AS createTime, updated_at AS updateTime, " +
            "update_by AS updateBy, deleted " +
            "FROM activities WHERE status = #{status} AND deleted = 0 ORDER BY id DESC")
    List<Activity> findByStatus(@Param("status") Integer status);

    /**
     * 根据serviceId查询活动
     */
    @Select("SELECT id, title, subtitle, region, description, cover_image AS coverImage, " +
            "start_time AS startTime, end_time AS endTime, status, vote_limit AS voteLimit, " +
            "group_count AS groupCount, sms_weight AS smsWeight, ivr_weight AS ivrWeight, " +
            "web_weight AS webWeight, app_weight AS appWeight, wechat_weight AS wechatWeight, " +
            "pay_weight AS payWeight, ip_limit AS ipLimit, enable_captcha AS enableCaptcha, " +
            "show_votes AS showVotes, domain, template, rules, service_id AS serviceId, " +
            "candidate_count AS candidateCount, total_votes AS totalVotes, " +
            "daily_candidate_limit AS dailyCandidateLimit, " +
            "candidate_daily_limit AS candidateDailyLimit, " +
            "created_by AS createBy, created_at AS createTime, updated_at AS updateTime, " +
            "update_by AS updateBy, deleted " +
            "FROM activities WHERE service_id = #{serviceId} AND deleted = 0 LIMIT 1")
    Activity findByServiceId(@Param("serviceId") String serviceId);

    /**
     * 创建活动
     */
    @Insert("INSERT INTO activities (title, subtitle, region, description, cover_image, " +
            "start_time, end_time, status, vote_limit, group_count, sms_weight, ivr_weight, " +
            "web_weight, app_weight, wechat_weight, pay_weight, ip_limit, enable_captcha, " +
            "show_votes, domain, template, rules, service_id, candidate_count, total_votes, " +
            "daily_candidate_limit, candidate_daily_limit, " +
            "created_by, created_at, updated_at, deleted) " +
            "VALUES (#{title}, #{subtitle}, #{region}, #{description}, #{coverImage}, " +
            "#{startTime}, #{endTime}, #{status}, #{voteLimit}, #{groupCount}, #{smsWeight}, #{ivrWeight}, " +
            "#{webWeight}, #{appWeight}, #{wechatWeight}, #{payWeight}, #{ipLimit}, #{enableCaptcha}, " +
            "#{showVotes}, #{domain}, #{template}, #{rules}, #{serviceId}, 0, 0, " +
            "#{dailyCandidateLimit}, #{candidateDailyLimit}, " +
            "#{createBy}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Activity activity);

    /**
     * 更新活动
     */
    @Update("UPDATE activities SET " +
            "title = #{title}, " +
            "subtitle = #{subtitle}, " +
            "region = #{region}, " +
            "description = #{description}, " +
            "cover_image = #{coverImage}, " +
            "start_time = #{startTime}, " +
            "end_time = #{endTime}, " +
            "status = #{status}, " +
            "vote_limit = #{voteLimit}, " +
            "group_count = #{groupCount}, " +
            "sms_weight = #{smsWeight}, " +
            "ivr_weight = #{ivrWeight}, " +
            "web_weight = #{webWeight}, " +
            "app_weight = #{appWeight}, " +
            "wechat_weight = #{wechatWeight}, " +
            "pay_weight = #{payWeight}, " +
            "ip_limit = #{ipLimit}, " +
            "enable_captcha = #{enableCaptcha}, " +
            "show_votes = #{showVotes}, " +
            "domain = #{domain}, " +
            "template = #{template}, " +
            "rules = #{rules}, " +
            "daily_candidate_limit = #{dailyCandidateLimit}, " +
            "candidate_daily_limit = #{candidateDailyLimit}, " +
            "update_by = #{updateBy}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    int update(Activity activity);

    /**
     * 删除活动（逻辑删除）
     */
    @Delete("DELETE FROM activities WHERE id = #{id}")
    int delete(@Param("id") Long id);

    /**
     * 更新活动统计信息
     */
    @Update("UPDATE activities SET " +
            "candidate_count = #{candidateCount}, " +
            "total_votes = #{totalVotes}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateStats(@Param("id") Long id, @Param("candidateCount") Integer candidateCount, @Param("totalVotes") Integer totalVotes);
}
