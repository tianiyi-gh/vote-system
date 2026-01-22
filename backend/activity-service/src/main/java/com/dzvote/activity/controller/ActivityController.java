package com.dzvote.activity.controller;

import com.dzvote.activity.entity.Activity;
import com.dzvote.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "活动管理", description = "活动相关接口")
public class ActivityController {
    
    private final ActivityService activityService;
    
    @GetMapping
    @Operation(summary = "获取活动列表")
    public ResponseEntity<List<Activity>> listActivities(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        try {
            List<Activity> activities = activityService.listActivities(page, size);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            log.error("获取活动列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取活动详情")
    public ResponseEntity<Activity> getActivity(@PathVariable Long id) {
        try {
            Activity activity = activityService.getActivity(id);
            if (activity != null) {
                return ResponseEntity.ok(activity);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("获取活动详情失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    @Operation(summary = "创建活动")
    public ResponseEntity<Activity> createActivity(@RequestBody Activity activity) {
        try {
            Activity saved = activityService.createActivity(activity);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("创建活动失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新活动")
    public ResponseEntity<Activity> updateActivity(
            @PathVariable Long id, 
            @RequestBody Activity activity) {
        try {
            activity.setId(id);
            Activity updated = activityService.updateActivity(activity);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("更新活动失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除活动")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        try {
            boolean deleted = activityService.deleteActivity(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("删除活动失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}/candidates")
    @Operation(summary = "获取活动候选人列表")
    public ResponseEntity<List<Object>> getCandidates(@PathVariable Long id) {
        try {
            // 临时返回模拟数据
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("获取候选人列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}