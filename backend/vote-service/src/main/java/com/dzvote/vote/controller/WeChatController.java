package com.dzvote.vote.controller;

import com.dzvote.vote.service.WeChatService;
import com.dzvote.vote.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信 JSSDK 接口
 */
@Slf4j
@Tag(name = "微信 JSSDK")
@RestController
@RequestMapping("/api/wechat")
@RequiredArgsConstructor
public class WeChatController {

    private final WeChatService weChatService;

    @Operation(summary = "获取 JSSDK 配置")
    @PostMapping("/jsapi-config")
    public Result<Map<String, String>> getJsApiConfig(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isEmpty()) {
            return Result.error("URL 不能为空");
        }

        // 去除 URL 中的 # 及其后面的部分
        url = url.split("#")[0];

        Map<String, String> config = weChatService.generateJsApiSignature(url);
        if (config == null) {
            return Result.error("获取 JSSDK 配置失败");
        }

        return Result.success(config);
    }
}
