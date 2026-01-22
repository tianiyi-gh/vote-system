package com.dzvote.voting.mapper;

import com.dzvote.voting.entity.VoteLimit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 投票限制数据访问层
 */
@Mapper
public interface VoteLimitMapper {
    
    /**
     * 根据活动ID和手机号查询
     */
    VoteLimit selectByActivityAndPhone(@Param("activityId") Long activityId, 
                                      @Param("phone") String phone);
    
    /**
     * 插入投票限制
     */
    int insert(VoteLimit limit);
    
    /**
     * 更新投票限制
     */
    int updateById(VoteLimit limit);
}
