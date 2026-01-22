package com.dzvote.activity.controller;

import com.dzvote.activity.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@Tag(name = "测试接口", description = "系统测试相关接口")
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "activity-service");
        data.put("version", "2.0.0");
        return Result.success("服务运行正常", data);
    }

    @GetMapping("/info")
    @Operation(summary = "服务信息", description = "获取服务基本信息")
    public Result<Map<String, Object>> info() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "DZVote Activity Service");
        data.put("description", "投票活动管理服务");
        data.put("version", "2.0.0");
        data.put("java", System.getProperty("java.version"));
        return Result.success(data);
    }
}