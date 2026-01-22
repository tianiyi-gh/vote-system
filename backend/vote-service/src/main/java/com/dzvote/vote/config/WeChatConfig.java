package com.dzvote.vote.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WeChatConfig {

    /**
     * 公众号 AppID
     */
    private String appId;

    /**
     * 公众号 AppSecret
     */
    private String appSecret;

    /**
     * JS 接口安全域名
     */
    private String jsApiDomain;

    /**
     * access_token 缓存时间（秒），默认 7200 秒
     */
    private Integer tokenExpireTime = 7200;

    /**
     * jsapi_ticket 缓存时间（秒），默认 7200 秒
     */
    private Integer ticketExpireTime = 7200;
}
