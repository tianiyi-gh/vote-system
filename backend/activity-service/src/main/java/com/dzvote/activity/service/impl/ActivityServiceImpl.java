package com.dzvote.activity.service.impl;

import com.dzvote.activity.dto.ActivityQueryRequest;
import com.dzvote.activity.entity.Activity;
import com.dzvote.activity.mapper.ActivityMapper;
import com.dzvote.activity.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    
    private final ActivityMapper activityMapper;
    
    @Override
    public List<Activity> listActivities(Integer page, Integer size) {
        try {
            ActivityQueryRequest request = new ActivityQueryRequest();
            request.setPage(page != null && page > 0 ? (page - 1) * size : 0);
            request.setSize(size != null && size > 0 ? size : 10);
            List<Activity> activities = activityMapper.selectList(request);
            log.info("查询活动列表成功，共{}条", activities.size());
            return activities;
        } catch (Exception e) {
            log.error("获取活动列表失败", e);
            throw new RuntimeException("获取活动列表失败", e);
        }
    }
    
    @Override
    public Activity getActivity(Long id) {
        try {
            Activity activity = activityMapper.selectById(id);
            if (activity != null) {
                log.info("查询活动详情成功: ID={}", id);
            } else {
                log.warn("活动不存在: ID={}", id);
            }
            return activity;
        } catch (Exception e) {
            log.error("获取活动详情失败", e);
            throw new RuntimeException("获取活动详情失败", e);
        }
    }
    
    @Override
    public Activity createActivity(Activity activity) {
        try {
            activity.setCreatedAt(LocalDateTime.now());
            activity.setUpdatedAt(LocalDateTime.now());
            activity.setDeleted(0);
            activity.setTotalVotes(0L);
            activity.setCandidateCount(0);
            if (activity.getStatus() == null) {
                activity.setStatus(0);
            }
            if (activity.getVoteLimit() == null) {
                activity.setVoteLimit(1);
            }
            int result = activityMapper.insert(activity);
            if (result > 0) {
                log.info("创建活动成功: ID={}, 标题={}", activity.getId(), activity.getTitle());
                return activity;
            } else {
                throw new RuntimeException("创建活动失败");
            }
        } catch (Exception e) {
            log.error("创建活动失败", e);
            throw new RuntimeException("创建活动失败", e);
        }
    }
    
    @Override
    public Activity updateActivity(Activity activity) {
        try {
            activity.setUpdatedAt(LocalDateTime.now());
            int result = activityMapper.updateById(activity);
            if (result > 0) {
                log.info("更新活动成功: ID={}, 标题={}", activity.getId(), activity.getTitle());
                return activityMapper.selectById(activity.getId());
            } else {
                log.warn("活动不存在或更新失败: ID={}", activity.getId());
                return null;
            }
        } catch (Exception e) {
            log.error("更新活动失败", e);
            throw new RuntimeException("更新活动失败", e);
        }
    }
    
    @Override
    public boolean deleteActivity(Long id) {
        try {
            int result = activityMapper.deleteById(id);
            if (result > 0) {
                log.info("删除活动成功: ID={}", id);
                return true;
            } else {
                log.warn("活动不存在或删除失败: ID={}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("删除活动失败", e);
            throw new RuntimeException("删除活动失败", e);
        }
    }
}