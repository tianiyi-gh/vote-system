package com.dzvote.vote.controller;

import com.dzvote.vote.annotation.IpLimit;
import com.dzvote.vote.annotation.RateLimit;
import com.dzvote.vote.dto.VoteRequest;
import com.dzvote.vote.dto.VoteResult;
import com.dzvote.vote.entity.Candidate;
import com.dzvote.vote.entity.VoteRecord;
import com.dzvote.vote.mapper.CandidateMapper;
import com.dzvote.vote.mapper.VoteRecordMapper;
import com.dzvote.vote.service.VoteService;
import com.dzvote.vote.util.EncodingUtils;
import com.dzvote.vote.util.Result;
import com.dzvote.vote.mapper.VoteRecordMapper.VoteRecordStats;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 投票控制器
 */
@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;
    private final CandidateMapper candidateMapper;
    private final VoteRecordMapper voteRecordMapper;

    /**
     * 获取真实客户端IP地址
     * 支持代理、负载均衡等场景
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多个代理，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 投票接口
     * 基于活动的投票限制（每个活动可配置vote_limit参数）
     * 默认：每天每人最多投10票（可通过活动配置调整）
     */
    @PostMapping
    @RateLimit(key = "vote", time = 60, count = 100, message = "投票请求过于频繁，请稍后再试")
    public Result<String> vote(@RequestBody VoteRequest request, HttpServletRequest httpRequest) {
        try {
            // 从请求头获取真实IP
            String realIp = getClientIp(httpRequest);
            request.setVoterIp(realIp);

            VoteResult result = voteService.vote(request);
            if (result.isSuccess()) {
                return Result.success("投票成功: " + result.getData());
            } else {
                return Result.error(result.getErrorMessage());
            }
        } catch (Exception e) {
            return Result.error("投票失败: " + e.getMessage());
        }
    }

    /**
     * 获取候选人列表
     */
    @GetMapping("/candidates")
    public Result<List<Candidate>> getCandidates(@RequestParam Long activityId) {
        try {
            List<Candidate> candidates = candidateMapper.findByActivityId(activityId);

            // 调试编码问题
            for (Candidate c : candidates) {
                String name = c.getName();
                System.out.println("========== 原始数据 ==========");
                System.out.println("name: " + name);
                System.out.println("name bytes(UTF-8): " + bytesToHex(name.getBytes(StandardCharsets.UTF_8)));
                System.out.println("name bytes(ISO-8859-1): " + bytesToHex(name.getBytes(StandardCharsets.ISO_8859_1)));

                // 尝试1: ISO-8859-1 -> UTF-8
                try {
                    String fixed1 = new String(name.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    System.out.println("尝试1 (ISO->UTF8): " + fixed1);
                    System.out.println("尝试1 bytes: " + bytesToHex(fixed1.getBytes(StandardCharsets.UTF_8)));
                    if (isValidChinese(fixed1)) {
                        System.out.println(">>> 使用尝试1");
                        c.setName(fixed1);
                    }
                } catch (Exception e) {
                    System.out.println("尝试1失败: " + e.getMessage());
                }

                String desc = c.getDescription();
                System.out.println("desc: " + desc);
                System.out.println("desc bytes(UTF-8): " + bytesToHex(desc.getBytes(StandardCharsets.UTF_8)));

                // 尝试1: ISO-8859-1 -> UTF-8
                try {
                    String fixed1 = new String(desc.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    System.out.println("尝试1 (ISO->UTF8): " + fixed1);
                    if (isValidChinese(fixed1)) {
                        System.out.println(">>> 使用尝试1");
                        c.setDescription(fixed1);
                    }
                } catch (Exception e) {
                    System.out.println("尝试1失败: " + e.getMessage());
                }
            }

            return Result.success(candidates);
        } catch (Exception e) {
            return Result.error("获取候选人列表失败: " + e.getMessage());
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private boolean isValidChinese(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        int chineseCount = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c >= 0x4E00 && c <= 0x9FA5) ||
                (c >= 0x3400 && c <= 0x4DBF)) {
                chineseCount++;
            }
        }
        return chineseCount > 0;
    }

    /**
     * 查询投票记录
     */
    @GetMapping("/records")
    public Result<List<VoteRecord>> getVoteRecords(@RequestParam Long activityId,
                                                      @RequestParam(defaultValue = "0") Integer page,
                                                      @RequestParam(defaultValue = "20") Integer size) {
        try {
            int offset = page * size;
            List<VoteRecord> records = voteRecordMapper.findVoteRecords(activityId, offset, size);
            System.out.println("====== 投票记录查询结果 ======");
            if (!records.isEmpty()) {
                VoteRecord first = records.get(0);
                System.out.println("第一条记录:");
                System.out.println("  id: " + first.getId());
                System.out.println("  candidateId: " + first.getCandidateId());
                System.out.println("  candidateName: " + first.getCandidateName());
                System.out.println("  candidateAvatar: " + first.getCandidateAvatar());
                System.out.println("  candidateNo: " + first.getCandidateNo());
            }
            System.out.println("总记录数: " + records.size());
            return Result.success(records);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询投票记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取候选人统计信息
     */
    @GetMapping("/stats/candidate/{candidateId}")
    public Result<VoteRecordStats> getCandidateStats(@PathVariable Long candidateId) {
        try {
            VoteRecordStats stats = voteRecordMapper.countCandidateVotes(candidateId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动每日统计
     */
    @GetMapping("/stats/activity/{activityId}")
    public Result<Map<String, Object>> getActivityStats(@PathVariable Long activityId,
                                                   @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate date) {
        try {
            VoteRecordStats stats = voteRecordMapper.countActivityDailyVotes(activityId, date);
            int uniqueVoters = voteRecordMapper.countActivityUniqueVoters(activityId, date);
            Map<String, Object> result = Map.of(
                "totalVotes", stats.getTotalVotes(),
                "smsVotes", stats.getSmsVotes(),
                "ivrVotes", stats.getIvrVotes(),
                "webVotes", stats.getWebVotes(),
                "appVotes", stats.getAppVotes(),
                "wechatVotes", stats.getWechatVotes(),
                "payVotes", stats.getPayVotes(),
                "uniqueVoters", uniqueVoters
            );
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取活动统计失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Vote Service is running");
    }

    /**
     * 获取用户投票记录（基于手机号或IP）
     */
    @GetMapping("/my-records")
    public Result<List<VoteRecord>> getMyVoteRecords(
            @RequestParam Long activityId,
            @RequestParam(required = false) String voterPhone,
            @RequestParam(required = false) String voterIp,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        try {
            // 如果没有提供手机号或IP，从请求中获取
            if (voterPhone == null && voterIp == null) {
                voterIp = getClientIp(request);
            }

            int offset = page * size;
            List<VoteRecord> records;

            if (voterPhone != null && !voterPhone.isEmpty()) {
                // 根据手机号查询
                records = voteRecordMapper.findVoteRecordsByPhone(activityId, voterPhone, offset, size);
            } else {
                // 根据IP查询
                records = voteRecordMapper.findVoteRecordsByIp(activityId, voterIp, offset, size);
            }

            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询投票记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户今日投票统计
     */
    @GetMapping("/my-stats")
    public Result<Map<String, Object>> getMyVoteStats(
            @RequestParam Long activityId,
            @RequestParam(required = false) String voterPhone,
            @RequestParam(required = false) String voterIp,
            HttpServletRequest request) {
        try {
            // 如果没有提供手机号或IP，从请求中获取
            if (voterPhone == null && voterIp == null) {
                voterIp = getClientIp(request);
            }

            Map<String, Object> stats;

            if (voterPhone != null && !voterPhone.isEmpty()) {
                // 根据手机号统计
                stats = voteRecordMapper.countUserVoteStatsByPhone(activityId, voterPhone);
            } else {
                // 根据IP统计
                stats = voteRecordMapper.countUserVoteStatsByIp(activityId, voterIp);
            }

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("查询投票统计失败: " + e.getMessage());
        }
    }
}