package com.dzvote.vote.mapper;

import com.dzvote.vote.entity.Candidate;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 候选人Mapper
 */
@Mapper
public interface CandidateMapper {

    /**
     * 根据活动ID查询候选人列表
     * status字段说明：0-未开始，1-正常，2-禁用
     */
    @Select("SELECT id, activity_id, service_id AS serviceId, candidate_no AS candidateNo, " +
            "name, group_name AS groupName, description, avatar, " +
            "votes, sms_votes AS smsVotes, ivr_votes AS ivrVotes, web_votes AS webVotes, " +
            "app_votes AS appVotes, wechat_votes AS wechatVotes, pay_votes AS payVotes, " +
            "total_votes AS totalVotes, score, ranking, " +
            "order_num AS orderNum, status, created_at AS createTime, updated_at AS updateTime " +
            "FROM candidates WHERE activity_id = #{activityId} AND deleted = 0 ORDER BY total_votes DESC")
    List<Candidate> findByActivityId(@Param("activityId") Long activityId);

    /**
     * 查询所有候选人
     */
    @Select("SELECT id, activity_id, service_id AS serviceId, candidate_no AS candidateNo, " +
            "name, group_name AS groupName, description, avatar, " +
            "votes, sms_votes AS smsVotes, ivr_votes AS ivrVotes, web_votes AS webVotes, " +
            "app_votes AS appVotes, wechat_votes AS wechatVotes, pay_votes AS payVotes, " +
            "total_votes AS totalVotes, score, ranking, " +
            "order_num AS orderNum, status, created_at AS createTime, updated_at AS updateTime " +
            "FROM candidates WHERE deleted = 0 ORDER BY id DESC")
    List<Candidate> findAll();

    /**
     * 根据ID查询候选人
     */
    @Select("SELECT id, activity_id AS activityId, service_id AS serviceId, candidate_no AS candidateNo, " +
            "name, group_name AS groupName, description, avatar, " +
            "votes, sms_votes AS smsVotes, ivr_votes AS ivrVotes, web_votes AS webVotes, " +
            "app_votes AS appVotes, wechat_votes AS wechatVotes, pay_votes AS payVotes, " +
            "total_votes AS totalVotes, score, ranking, " +
            "order_num AS orderNum, status, created_at AS createTime, updated_at AS updateTime " +
            "FROM candidates WHERE id = #{id} AND deleted = 0")
    Candidate findById(@Param("id") Long id);

    /**
     * 创建候选人
     */
    @Insert("INSERT INTO candidates (activity_id, service_id, candidate_no, name, description, avatar, votes, order_num, group_name, status, deleted, created_at, updated_at) " +
            "VALUES (#{activityId}, #{serviceId}, #{candidateNo}, #{name}, #{description}, #{avatar}, #{votes}, #{orderNum}, #{groupName}, #{status}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Candidate candidate);

    /**
     * 更新候选人
     */
    @Update("UPDATE candidates SET " +
            "candidate_no = #{candidateNo}, " +
            "service_id = #{serviceId}, " +
            "name = #{name}, " +
            "description = #{description}, " +
            "avatar = #{avatar}, " +
            "votes = #{votes}, " +
            "order_num = #{orderNum}, " +
            "group_name = #{groupName}, " +
            "status = #{status}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    int update(Candidate candidate);

    /**
     * 删除候选人（逻辑删除）
     */
    @Update("UPDATE candidates SET deleted = 1, updated_at = NOW() WHERE id = #{id}")
    int delete(@Param("id") Long id);

    /**
     * 根据活动ID查询候选人总数
     */
    @Select("SELECT COUNT(*) FROM candidates WHERE activity_id = #{activityId} AND deleted = 0")
    Integer countByActivityId(@Param("activityId") Long activityId);

    /**
     * 批量插入候选人
     */
    @Insert("<script>" +
            "INSERT INTO candidates (activity_id, service_id, candidate_no, name, description, avatar, votes, order_num, group_name, status, deleted, created_at, updated_at) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.activityId}, #{item.serviceId}, #{item.candidateNo}, #{item.name}, #{item.description}, #{item.avatar}, 0, #{item.orderNum}, #{item.groupName}, #{item.status}, 0, NOW(), NOW())" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("list") List<Candidate> list);
}
