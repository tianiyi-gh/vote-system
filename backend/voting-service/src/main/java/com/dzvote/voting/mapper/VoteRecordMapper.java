package com.dzvote.voting.mapper;

import com.dzvote.voting.entity.VoteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 投票记录数据访问层
 */
@Mapper
public interface VoteRecordMapper {
    
    /**
     * 插入投票记录
     */
    int insert(VoteRecord record);
}
