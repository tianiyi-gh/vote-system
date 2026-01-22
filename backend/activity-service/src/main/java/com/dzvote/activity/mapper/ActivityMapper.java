package com.dzvote.activity.mapper;

import com.dzvote.activity.entity.Activity;
import com.dzvote.activity.dto.ActivityQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 活动数据访问层
 */
@Mapper
public interface ActivityMapper {
    
    /**
     * 查询活动列表
     */
    List<Activity> selectList(@Param("request") ActivityQueryRequest request);
    
    /**
     * 根据ID查询活动
     */
    Activity selectById(@Param("id") Long id);
    
    /**
     * 插入活动
     */
    int insert(Activity activity);
    
    /**
     * 更新活动
     */
    int updateById(Activity activity);
    
    /**
     * 删除活动
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 查询活动总数
     */
    Long selectCount(@Param("request") ActivityQueryRequest request);
}