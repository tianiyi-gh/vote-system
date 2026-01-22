package com.dzvote.gateway.service;

import java.util.Map;

/**
 * 微信服务接口
 */
public interface WeChatService {

    /**
     * 获取微信用户信息
     * @param code 微信授权码
     * @return 用户信息
     */
    Map<String, Object> getWeChatUserInfo(String code);

    /**
     * 发送微信模板消息
     * @param openid 用户openid
     * @param templateId 模板ID
     * @param data 模板数据
     * @return 发送结果
     */
    boolean sendTemplateMessage(String openid, String templateId, Map<String, Object> data);

    /**
     * 获取微信访问令牌
     * @return access_token
     */
    String getAccessToken();

    /**
     * 验证微信签名
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param token 令牌
     * @return 是否验证通过
     */
    boolean verifySignature(String signature, String timestamp, String nonce, String token);

    /**
     * 处理微信消息
     * @param xmlMessage 微信XML消息
     * @return 响应消息
     */
    String handleMessage(String xmlMessage);
}