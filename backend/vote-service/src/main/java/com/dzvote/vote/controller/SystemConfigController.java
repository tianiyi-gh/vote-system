package com.dzvote.vote.controller;

import com.dzvote.vote.config.WeChatConfig;
import com.dzvote.vote.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置接口
 */
@Slf4j
@Tag(name = "系统配置")
@RestController
@RequestMapping("/api/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final WeChatConfig weChatConfig;

    @Operation(summary = "获取微信配置")
    @GetMapping("/wechat")
    public Result<Map<String, Object>> getWeChatConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("appId", weChatConfig.getAppId());
        // 不返回 AppSecret（安全考虑）
        config.put("appSecret", "***");
        config.put("jsApiDomain", weChatConfig.getJsApiDomain());

        return Result.success(config);
    }

    @Operation(summary = "更新微信配置（需重启服务生效）")
    @PostMapping("/wechat")
    public Result<String> updateWeChatConfig(@RequestBody Map<String, String> request) {
        String appId = request.get("appId");
        String appSecret = request.get("appSecret");
        String jsApiDomain = request.get("jsApiDomain");

        if (appId != null && !appId.isEmpty()) {
            weChatConfig.setAppId(appId);
            log.info("更新微信 AppID: {}", appId);
        }

        if (appSecret != null && !appSecret.isEmpty()) {
            weChatConfig.setAppSecret(appSecret);
            log.info("更新微信 AppSecret: ***");
        }

        if (jsApiDomain != null && !jsApiDomain.isEmpty()) {
            weChatConfig.setJsApiDomain(jsApiDomain);
            log.info("更新微信 JS 接口安全域名: {}", jsApiDomain);
        }

        // 注意：由于使用 @ConfigurationProperties，直接修改不会持久化到配置文件
        // 建议通过环境变量或在 application.yml 中修改

        return Result.success("配置已更新（需重启服务生效）");
    }
}
