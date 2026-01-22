package com.dzvote.voting.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 候选人数据访问层（投票服务专用）
 */
@Mapper
public interface CandidateMapper {
    
    /**
     * 增加票数
     */
    int incrementVotes(@Param("id") Long id);
    
    /**
     * 查询票数
     */
    Integer selectVotesById(@Param("id") Long id);
}
