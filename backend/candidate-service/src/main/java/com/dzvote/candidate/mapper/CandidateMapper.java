package com.dzvote.candidate.mapper;

import com.dzvote.candidate.entity.Candidate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 候选人数据访问层
 */
@Mapper
public interface CandidateMapper {
    
    /**
     * 查询候选人列表
     */
    List<Candidate> selectList(@Param("activityId") Long activityId, 
                              @Param("page") Integer page, 
                              @Param("size") Integer size);
    
    /**
     * 根据ID查询候选人
     */
    Candidate selectById(@Param("id") Long id);
    
    /**
     * 插入候选人
     */
    int insert(Candidate candidate);
    
    /**
     * 更新候选人
     */
    int updateById(Candidate candidate);
    
    /**
     * 删除候选人
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 查询活动的service_id
     */
    @Select("SELECT service_id FROM activities WHERE id = #{activityId}")
    String selectServiceIdByActivityId(@Param("activityId") Long activityId);
}
