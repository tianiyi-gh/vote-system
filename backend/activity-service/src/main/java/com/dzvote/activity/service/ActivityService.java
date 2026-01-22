package com.dzvote.activity.service;

import com.dzvote.activity.entity.Activity;
import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {
    
    /**
     * 获取活动列表
     */
    List<Activity> listActivities(Integer page, Integer size);
    
    /**
     * 获取活动详情
     */
    Activity getActivity(Long id);
    
    /**
     * 创建活动
     */
    Activity createActivity(Activity activity);
    
    /**
     * 更新活动
     */
    Activity updateActivity(Activity activity);
    
    /**
     * 删除活动
     */
    boolean deleteActivity(Long id);
}