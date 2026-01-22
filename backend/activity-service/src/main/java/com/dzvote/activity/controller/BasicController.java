package com.dzvote.activity.controller;

import com.dzvote.activity.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础测试控制器
 */
@Tag(name = "基础测试", description = "系统基础测试接口")
@RestController
@RequestMapping
public class BasicController {

    @GetMapping("/")
    @Operation(summary = "欢迎页面", description = "默认欢迎信息")
    public Result<Map<String, Object>> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "DZVote Activity Service");
        data.put("status", "运行中");
        data.put("timestamp", LocalDateTime.now());
        data.put("version", "2.0.0");
        return Result.success("服务启动成功", data);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "系统健康状态检查")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("service", "activity-service");
        data.put("uptime", System.currentTimeMillis());
        return Result.success("系统健康", data);
    }

    @GetMapping("/info")
    @Operation(summary = "服务信息", description = "获取服务详细信息")
    public Result<Map<String, Object>> info() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "DZVote Activity Service");
        data.put("description", "投票活动管理服务");
        data.put("version", "2.0.0");
        data.put("java", System.getProperty("java.version"));
        data.put("os", System.getProperty("os.name"));
        data.put("memory", Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB");
        return Result.success(data);
    }

    @PostMapping("/echo")
    @Operation(summary = "回显测试", description = "POST请求测试")
    public Result<Map<String, Object>> echo(@RequestBody Map<String, Object> request) {
        Map<String, Object> data = new HashMap<>();
        data.put("received", request);
        data.put("timestamp", LocalDateTime.now());
        data.put("server", "activity-service");
        return Result.success("回显成功", data);
    }
}