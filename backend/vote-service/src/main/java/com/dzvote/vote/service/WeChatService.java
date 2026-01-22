package com.dzvote.vote.service;

import com.dzvote.vote.config.WeChatConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 微信 JSSDK 服务
 */
@Slf4j
@Service
public class WeChatService {

    private final WeChatConfig weChatConfig;
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis key 前缀
    private static final String ACCESS_TOKEN_KEY = "wechat:access_token";
    private static final String JSAPI_TICKET_KEY = "wechat:jsapi_ticket";

    public WeChatService(WeChatConfig weChatConfig, RestTemplate restTemplate,
                         RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.weChatConfig = weChatConfig;
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取 access_token
     */
    public String getAccessToken() {
        // 先从缓存获取
        String token = redisTemplate.opsForValue().get(ACCESS_TOKEN_KEY);
        if (StringUtils.hasText(token)) {
            return token;
        }

        // 缓存不存在，从微信服务器获取
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
                     "&appid=" + weChatConfig.getAppId() +
                     "&secret=" + weChatConfig.getAppSecret();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String accessToken = root.get("access_token").asText();
                int expiresIn = root.get("expires_in").asInt();

                // 缓存 access_token，提前 5 分钟过期
                redisTemplate.opsForValue().set(ACCESS_TOKEN_KEY, accessToken,
                    Math.max(expiresIn - 300, 3600));

                log.info("获取 access_token 成功");
                return accessToken;
            }
        } catch (Exception e) {
            log.error("获取 access_token 失败", e);
        }

        return null;
    }

    /**
     * 获取 jsapi_ticket
     */
    public String getJsApiTicket() {
        // 先从缓存获取
        String ticket = redisTemplate.opsForValue().get(JSAPI_TICKET_KEY);
        if (StringUtils.hasText(ticket)) {
            return ticket;
        }

        // 获取 access_token
        String accessToken = getAccessToken();
        if (accessToken == null) {
            return null;
        }

        // 从微信服务器获取 jsapi_ticket
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" +
                     accessToken + "&type=jsapi";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String jsapiTicket = root.get("ticket").asText();
                int expiresIn = root.get("expires_in").asInt();

                // 缓存 jsapi_ticket，提前 5 分钟过期
                redisTemplate.opsForValue().set(JSAPI_TICKET_KEY, jsapiTicket,
                    Math.max(expiresIn - 300, 3600));

                log.info("获取 jsapi_ticket 成功");
                return jsapiTicket;
            }
        } catch (Exception e) {
            log.error("获取 jsapi_ticket 失败", e);
        }

        return null;
    }

    /**
     * 生成 JSSDK 签名
     * @param url 当前页面的 URL（不包含 # 及其后面的部分）
     */
    public Map<String, String> generateJsApiSignature(String url) {
        String jsApiTicket = getJsApiTicket();
        if (jsApiTicket == null) {
            return null;
        }

        // 生成随机字符串 nonceStr
        String nonceStr = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 生成时间戳 timestamp
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        // 生成签名 signature
        String signature = generateSignature(jsApiTicket, nonceStr, timestamp, url);

        Map<String, String> config = new HashMap<>();
        config.put("appId", weChatConfig.getAppId());
        config.put("timestamp", timestamp);
        config.put("nonceStr", nonceStr);
        config.put("signature", signature);
        config.put("jsApiList", "updateAppMessageShareData,updateTimelineShareData,onMenuShareAppMessage,onMenuShareTimeline");

        log.info("生成 JSSDK 签名成功, url: {}", url);
        return config;
    }

    /**
     * 生成签名
     * signature = sha1(jsapi_ticket + noncestr + timestamp + url)
     */
    private String generateSignature(String jsApiTicket, String nonceStr, String timestamp, String url) {
        // 按字典序排序参数
        List<String> params = Arrays.asList(jsApiTicket, nonceStr, timestamp, url);
        params.sort(String::compareTo);

        // 拼接字符串
        String string1 = String.join("", params);

        // SHA1 加密
        return sha1(string1);
    }

    /**
     * SHA1 加密
     */
    private String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : digest) {
                String shaHex = Integer.toHexString(b & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA1 加密失败", e);
            return null;
        }
    }
}
