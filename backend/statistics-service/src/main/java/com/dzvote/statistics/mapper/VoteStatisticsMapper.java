package com.dzvote.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dzvote.statistics.entity.VoteStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投票统计Mapper
 */
@Mapper
public interface VoteStatisticsMapper extends BaseMapper<VoteStatistics> {
}