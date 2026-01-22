package com.dzvote.vote.controller;

import com.dzvote.vote.entity.Activity;
import com.dzvote.vote.entity.Candidate;
import com.dzvote.vote.mapper.ActivityMapper;
import com.dzvote.vote.mapper.CandidateMapper;
import com.dzvote.vote.mapper.VoteRecordMapper;
import com.dzvote.vote.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 活动管理控制器
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityMapper activityMapper;
    private final CandidateMapper candidateMapper;
    private final VoteRecordMapper voteRecordMapper;

    /**
     * 获取所有活动列表
     */
    @GetMapping
    public Result<List<Activity>> listActivities() {
        try {
            List<Activity> activities = activityMapper.findAll();
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error("获取活动列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询活动详情
     */
    @GetMapping("/{id}")
    public Result<Activity> getActivity(@PathVariable Long id) {
        try {
            Activity activity = activityMapper.findById(id);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            return Result.success(activity);
        } catch (Exception e) {
            return Result.error("获取活动详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据serviceId查询活动详情（用于H5前端）
     */
    @GetMapping("/service/{serviceId}")
    public Result<Activity> getActivityByServiceId(@PathVariable String serviceId) {
        try {
            Activity activity = activityMapper.findByServiceId(serviceId);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            return Result.success(activity);
        } catch (Exception e) {
            return Result.error("获取活动详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建新活动
     */
    @PostMapping
    public Result<String> createActivity(@RequestBody Activity activity) {
        try {
            // 设置默认值
            if (activity.getStatus() == null) {
                activity.setStatus(1); // 默认进行中
            }
            if (activity.getIpLimit() == null) {
                activity.setIpLimit(0); // 默认不限制
            }
            if (activity.getGroupCount() == null) {
                activity.setGroupCount(1);
            }
            // 设置默认权重
            if (activity.getSmsWeight() == null) activity.setSmsWeight(100);
            if (activity.getIvrWeight() == null) activity.setIvrWeight(100);
            if (activity.getWebWeight() == null) activity.setWebWeight(100);
            if (activity.getAppWeight() == null) activity.setAppWeight(100);
            if (activity.getWechatWeight() == null) activity.setWechatWeight(100);
            if (activity.getPayWeight() == null) activity.setPayWeight(100);
            if (activity.getEnableCaptcha() == null) activity.setEnableCaptcha(1);
            if (activity.getShowVotes() == null) activity.setShowVotes(1);

            int result = activityMapper.insert(activity);
            if (result > 0) {
                return Result.success("创建成功，活动ID: " + activity.getId());
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            return Result.error("创建活动失败: " + e.getMessage());
        }
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public Result<String> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        try {
            // 检查活动是否存在
            Activity existing = activityMapper.findById(id);
            if (existing == null) {
                return Result.error("活动不存在");
            }

            // 只更新提供的字段，保留原有字段
            if (activity.getTitle() != null) {
                existing.setTitle(activity.getTitle());
            }
            if (activity.getDescription() != null) {
                existing.setDescription(activity.getDescription());
            }
            if (activity.getCoverImage() != null) {
                existing.setCoverImage(activity.getCoverImage());
            }
            if (activity.getStartTime() != null) {
                existing.setStartTime(activity.getStartTime());
            }
            if (activity.getEndTime() != null) {
                existing.setEndTime(activity.getEndTime());
            }
            if (activity.getVoteLimit() != null) {
                existing.setVoteLimit(activity.getVoteLimit());
            }
            if (activity.getDailyCandidateLimit() != null) {
                existing.setDailyCandidateLimit(activity.getDailyCandidateLimit());
            }
            if (activity.getCandidateDailyLimit() != null) {
                existing.setCandidateDailyLimit(activity.getCandidateDailyLimit());
            }
            if (activity.getStatus() != null) {
                existing.setStatus(activity.getStatus());
            }
            if (activity.getSubtitle() != null) {
                existing.setSubtitle(activity.getSubtitle());
            }
            if (activity.getRegion() != null) {
                existing.setRegion(activity.getRegion());
            }
            if (activity.getDomain() != null) {
                existing.setDomain(activity.getDomain());
            }
            if (activity.getTemplate() != null) {
                existing.setTemplate(activity.getTemplate());
            }
            if (activity.getRules() != null) {
                existing.setRules(activity.getRules());
            }
            if (activity.getGroupCount() != null) {
                existing.setGroupCount(activity.getGroupCount());
            }
            if (activity.getIpLimit() != null) {
                existing.setIpLimit(activity.getIpLimit());
            }
            if (activity.getEnableCaptcha() != null) {
                existing.setEnableCaptcha(activity.getEnableCaptcha());
            }
            if (activity.getShowVotes() != null) {
                existing.setShowVotes(activity.getShowVotes());
            }
            if (activity.getSmsWeight() != null) {
                existing.setSmsWeight(activity.getSmsWeight());
            }
            if (activity.getIvrWeight() != null) {
                existing.setIvrWeight(activity.getIvrWeight());
            }
            if (activity.getWebWeight() != null) {
                existing.setWebWeight(activity.getWebWeight());
            }
            if (activity.getAppWeight() != null) {
                existing.setAppWeight(activity.getAppWeight());
            }
            if (activity.getWechatWeight() != null) {
                existing.setWechatWeight(activity.getWechatWeight());
            }
            if (activity.getPayWeight() != null) {
                existing.setPayWeight(activity.getPayWeight());
            }

            existing.setId(id);
            int result = activityMapper.update(existing);
            if (result > 0) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新活动失败: " + e.getMessage());
        }
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteActivity(@PathVariable Long id) {
        try {
            // 检查活动是否存在
            Activity existing = activityMapper.findById(id);
            if (existing == null) {
                return Result.error("活动不存在");
            }

            int result = activityMapper.delete(id);
            if (result > 0) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除活动失败: " + e.getMessage());
        }
    }

    /**
     * 复制活动
     */
    @PostMapping("/{id}/copy")
    public Result<String> copyActivity(@PathVariable Long id) {
        try {
            // 检查原活动是否存在
            Activity original = activityMapper.findById(id);
            if (original == null) {
                return Result.error("活动不存在");
            }

            // 创建新活动（复制原活动的所有字段，但排除ID和统计信息）
            Activity newActivity = new Activity();
            newActivity.setTitle(original.getTitle() + " (副本)");
            newActivity.setSubtitle(original.getSubtitle());
            newActivity.setRegion(original.getRegion());
            newActivity.setDescription(original.getDescription());
            newActivity.setCoverImage(original.getCoverImage());
            newActivity.setGroupCount(original.getGroupCount());
            newActivity.setVoteLimit(original.getVoteLimit());
            newActivity.setIpLimit(original.getIpLimit());
            newActivity.setEnableCaptcha(original.getEnableCaptcha());
            newActivity.setShowVotes(original.getShowVotes());
            newActivity.setDomain(original.getDomain());
            newActivity.setTemplate(original.getTemplate());
            newActivity.setRules(original.getRules());
            newActivity.setServiceId(original.getServiceId());

            // 复制权重设置
            newActivity.setSmsWeight(original.getSmsWeight());
            newActivity.setIvrWeight(original.getIvrWeight());
            newActivity.setWebWeight(original.getWebWeight());
            newActivity.setAppWeight(original.getAppWeight());
            newActivity.setWechatWeight(original.getWechatWeight());
            newActivity.setPayWeight(original.getPayWeight());

            // 设置状态为未开始
            newActivity.setStatus(0);

            // 插入新活动
            int result = activityMapper.insert(newActivity);
            if (result > 0) {
                return Result.success("复制成功，新活动ID: " + newActivity.getId());
            } else {
                return Result.error("复制失败");
            }
        } catch (Exception e) {
            return Result.error("复制活动失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态查询活动
     */
    @GetMapping("/status/{status}")
    public Result<List<Activity>> getActivitiesByStatus(@PathVariable Integer status) {
        try {
            List<Activity> activities = activityMapper.findByStatus(status);
            return Result.success(activities);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据活动ID获取候选人列表（投票端）
     */
    @GetMapping("/{id}/candidates")
    public Result<List<Candidate>> getCandidatesByActivity(@PathVariable Long id) {
        try {
            // 检查活动是否存在
            Activity activity = activityMapper.findById(id);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            // 只返回状态为1（正常）的候选人
            List<Candidate> candidates = candidateMapper.findByActivityId(id);
            List<Candidate> activeCandidates = candidates.stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .toList();
            return Result.success(activeCandidates);
        } catch (Exception e) {
            return Result.error("获取候选人列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据serviceId和candidateId获取候选人详情（投票端）
     */
    @GetMapping("/service/{serviceId}/candidates/{candidateId}")
    public Result<Candidate> getCandidateByServiceAndId(
            @PathVariable String serviceId,
            @PathVariable Long candidateId) {
        try {
            // 验证活动存在
            Activity activity = activityMapper.findByServiceId(serviceId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            // 获取候选人
            Candidate candidate = candidateMapper.findById(candidateId);
            if (candidate == null) {
                return Result.error("候选人不存在");
            }

            // 验证候选人属于该活动
            if (!candidate.getActivityId().equals(activity.getId())) {
                return Result.error("候选人不属于该活动");
            }

            return Result.success(candidate);
        } catch (Exception e) {
            return Result.error("获取候选人详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据serviceId获取排名候选人列表（支持排序和限制）
     */
    @GetMapping("/service/{serviceId}/candidates")
    public Result<List<Candidate>> getCandidatesByServiceIdSorted(
            @PathVariable String serviceId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) Integer limit) {
        try {
            Activity activity = activityMapper.findByServiceId(serviceId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            // 获取候选人
            List<Candidate> candidates = candidateMapper.findByActivityId(activity.getId());

            // 过滤正常状态(status=1)和非禁用(status!=2)
            List<Candidate> activeCandidates = candidates.stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .toList();

            // 排序
            if ("votes".equals(sortBy) || "totalVotes".equals(sortBy)) {
                if ("desc".equals(sortOrder) || sortOrder == null) {
                    activeCandidates.sort((a, b) -> Long.compare(
                        b.getTotalVotes() != null ? b.getTotalVotes() : 0,
                        a.getTotalVotes() != null ? a.getTotalVotes() : 0
                    ));
                } else {
                    activeCandidates.sort((a, b) -> Long.compare(
                        a.getTotalVotes() != null ? a.getTotalVotes() : 0,
                        b.getTotalVotes() != null ? b.getTotalVotes() : 0
                    ));
                }
            } else if ("ranking".equals(sortBy)) {
                if ("desc".equals(sortOrder) || sortOrder == null) {
                    activeCandidates.sort((a, b) -> Integer.compare(
                        b.getRanking() != null ? b.getRanking() : 999,
                        a.getRanking() != null ? a.getRanking() : 999
                    ));
                } else {
                    activeCandidates.sort((a, b) -> Integer.compare(
                        a.getRanking() != null ? a.getRanking() : 999,
                        b.getRanking() != null ? b.getRanking() : 999
                    ));
                }
            }

            // 限制返回数量
            if (limit != null && limit > 0) {
                activeCandidates = activeCandidates.stream()
                    .limit(limit)
                    .toList();
            }

            return Result.success(activeCandidates);
        } catch (Exception e) {
            return Result.error("获取候选人列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动排行榜（实时）
     */
    @GetMapping("/service/{serviceId}/ranking")
    public Result<List<Candidate>> getRanking(@PathVariable String serviceId) {
        try {
            Activity activity = activityMapper.findByServiceId(serviceId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            // 获取候选人并按票数排序
            List<Candidate> candidates = candidateMapper.findByActivityId(activity.getId());
            List<Candidate> activeCandidates = candidates.stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .sorted((a, b) -> Long.compare(
                    b.getTotalVotes() != null ? b.getTotalVotes() : 0,
                    a.getTotalVotes() != null ? a.getTotalVotes() : 0
                ))
                .toList();

            // 设置排名
            for (int i = 0; i < activeCandidates.size(); i++) {
                activeCandidates.get(i).setRanking(i + 1);
            }

            return Result.success(activeCandidates);
        } catch (Exception e) {
            return Result.error("获取排行榜失败: " + e.getMessage());
        }
    }

    /**
     * 获取活动每日排行榜
     */
    @GetMapping("/service/{serviceId}/ranking/daily")
    public Result<List<Map<String, Object>>> getDailyRanking(
            @PathVariable String serviceId,
            @RequestParam(required = false) String date) {
        try {
            Activity activity = activityMapper.findByServiceId(serviceId);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            // 使用指定日期或今天
            LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();

            // 获取每日投票统计
            List<Map<String, Object>> dailyStats = voteRecordMapper.getCandidateDailyRanking(
                activity.getId(), queryDate
            );

            return Result.success(dailyStats);
        } catch (Exception e) {
            return Result.error("获取每日排行榜失败: " + e.getMessage());
        }
    }
}
