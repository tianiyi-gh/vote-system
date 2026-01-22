package com.dzvote.gateway.controller;

import com.dzvote.gateway.service.SmsService;
import com.dzvote.gateway.service.WeChatService;
import com.dzvote.vote.dto.VoteRequest;
import com.dzvote.vote.dto.VoteResult;
import com.dzvote.vote.service.VoteService;
import com.dzvote.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 多渠道投票控制器
 */
@Slf4j
@Tag(name = "多渠道投票", description = "短信、微信等多渠道投票接口")
@RestController
@RequestMapping("/api/channel")
@RequiredArgsConstructor
public class ChannelVoteController {

    private final VoteService voteService;
    private final SmsService smsService;
    private final WeChatService weChatService;

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send-code")
    public Result<Boolean> sendSmsCode(@RequestParam String phone) {
        // 生成6位随机验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        boolean result = smsService.sendVoteCode(phone, code);
        
        if (result) {
            return Result.success("验证码发送成功", true);
        } else {
            return Result.failure("SEND_FAILED", "验证码发送失败，请稍后重试");
        }
    }

    @Operation(summary = "短信投票")
    @PostMapping("/sms/vote")
    public Result<VoteResult> smsVote(@RequestBody @Valid SmsVoteRequest request) {
        try {
            // 验证短信验证码
            if (!smsService.verifyCode(request.getPhone(), request.getCode())) {
                return Result.failure("INVALID_CODE", "验证码错误或已过期");
            }

            // 构建投票请求
            VoteRequest voteRequest = new VoteRequest();
            voteRequest.setActivityId(request.getActivityId());
            voteRequest.setCandidateId(request.getCandidateId());
            voteRequest.setChannel("SMS");
            voteRequest.setVoterPhone(request.getPhone());
            voteRequest.setVoterIp(getClientIP());
            
            // 进行投票
            VoteResult result = voteService.vote(voteRequest);
            
            if (result.getSuccess()) {
                // 发送投票确认短信
                smsService.sendVoteConfirm(request.getPhone(), request.getCandidateName());
            }
            
            return Result.success(result.getSuccess() ? "投票成功" : "投票失败", result);

        } catch (Exception e) {
            log.error("短信投票失败", e);
            return Result.failure("VOTE_FAILED", "投票失败，请稍后重试");
        }
    }

    @Operation(summary = "微信授权登录")
    @PostMapping("/wechat/auth")
    public Result<Map<String, Object>> wechatAuth(@RequestParam String code) {
        try {
            Map<String, Object> userInfo = weChatService.getWeChatUserInfo(code);
            
            if (userInfo != null && userInfo.containsKey("openid")) {
                return Result.success("授权成功", userInfo);
            } else {
                return Result.failure("AUTH_FAILED", "微信授权失败");
            }

        } catch (Exception e) {
            log.error("微信授权失败", e);
            return Result.failure("AUTH_ERROR", "授权出错，请稍后重试");
        }
    }

    @Operation(summary = "微信投票")
    @PostMapping("/wechat/vote")
    public Result<VoteResult> wechatVote(@RequestBody @Valid WeChatVoteRequest request) {
        try {
            // 构建投票请求
            VoteRequest voteRequest = new VoteRequest();
            voteRequest.setActivityId(request.getActivityId());
            voteRequest.setCandidateId(request.getCandidateId());
            voteRequest.setChannel("WECHAT");
            voteRequest.setVoterId(request.getOpenid());
            voteRequest.setVoterIp(getClientIP());
            
            // 进行投票
            VoteResult result = voteService.vote(voteRequest);
            
            return Result.success(result.getSuccess() ? "投票成功" : "投票失败", result);

        } catch (Exception e) {
            log.error("微信投票失败", e);
            return Result.failure("VOTE_FAILED", "投票失败，请稍后重试");
        }
    }

    @Operation(summary = "微信消息接收")
    @PostMapping("/wechat/message")
    public String wechatMessage(
            @RequestParam String signature,
            @RequestParam String timestamp,
            @RequestParam String nonce,
            @RequestParam String openid,
            @RequestBody String xmlMessage) {

        try {
            // 验证签名
            if (!weChatService.verifySignature(signature, timestamp, nonce, "your_token")) {
                return "签名验证失败";
            }

            // 处理消息
            String response = weChatService.handleMessage(xmlMessage);
            log.info("微信消息处理: openid={}, response={}", openid, response);
            
            return response;

        } catch (Exception e) {
            log.error("处理微信消息失败", e);
            return "处理失败";
        }
    }

    @Operation(summary = "APP投票")
    @PostMapping("/app/vote")
    public Result<VoteResult> appVote(@RequestBody @Valid AppVoteRequest request) {
        try {
            // 构建投票请求
            VoteRequest voteRequest = new VoteRequest();
            voteRequest.setActivityId(request.getActivityId());
            voteRequest.setCandidateId(request.getCandidateId());
            voteRequest.setChannel("APP");
            voteRequest.setVoterId(request.getUserId());
            voteRequest.setVoterIp(getClientIP());
            
            // 进行投票
            VoteResult result = voteService.vote(voteRequest);
            
            return Result.success(result.getSuccess() ? "投票成功" : "投票失败", result);

        } catch (Exception e) {
            log.error("APP投票失败", e);
            return Result.failure("VOTE_FAILED", "投票失败，请稍后重试");
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIP() {
        // 这里应该从请求头获取真实的客户端IP
        return "127.0.0.1";
    }

    // 内部请求类
    public static class SmsVoteRequest {
        private Long activityId;
        private Long candidateId;
        private String candidateName;
        private String phone;
        private String code;

        // getters and setters
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public Long getCandidateId() { return candidateId; }
        public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }
        public String getCandidateName() { return candidateName; }
        public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    public static class WeChatVoteRequest {
        private Long activityId;
        private Long candidateId;
        private String openid;

        // getters and setters
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public Long getCandidateId() { return candidateId; }
        public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
    }

    public static class AppVoteRequest {
        private Long activityId;
        private Long candidateId;
        private String userId;
        private String deviceId;

        // getters and setters
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public Long getCandidateId() { return candidateId; }
        public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    }
}