package com.dzvote.vote.controller;

import com.dzvote.vote.dto.CandidateBatchImportRequest;
import com.dzvote.vote.dto.CandidateImportItem;
import com.dzvote.vote.entity.Candidate;
import com.dzvote.vote.mapper.ActivityMapper;
import com.dzvote.vote.mapper.CandidateMapper;
import com.dzvote.vote.util.ExcelUtil;
import com.dzvote.vote.util.FileUploadUtil;
import com.dzvote.vote.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 候选人管理控制器（管理后台专用）
 */
@RestController
@RequestMapping("/api/admin/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateMapper candidateMapper;
    private final ActivityMapper activityMapper;

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 根据活动ID获取候选人列表（包括所有状态的）
     */
    @GetMapping
    public Result<List<Candidate>> listCandidates(@RequestParam Long activityId) {
        try {
            // 检查活动是否存在
            if (activityMapper.findById(activityId) == null) {
                return Result.error("活动不存在");
            }

            List<Candidate> candidates = candidateMapper.findByActivityId(activityId);
            return Result.success(candidates);
        } catch (Exception e) {
            return Result.error("获取候选人列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询候选人详情
     */
    @GetMapping("/{id}")
    public Result<Candidate> getCandidate(@PathVariable Long id) {
        try {
            Candidate candidate = candidateMapper.findById(id);
            if (candidate == null) {
                return Result.error("候选人不存在");
            }
            return Result.success(candidate);
        } catch (Exception e) {
            return Result.error("获取候选人详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建候选人
     */
    @PostMapping
    public Result<String> createCandidate(@RequestBody Candidate candidate) {
        try {
            // 检查活动是否存在
            if (activityMapper.findById(candidate.getActivityId()) == null) {
                return Result.error("活动不存在");
            }

            // 获取活动的serviceId并设置到候选人
            var activity = activityMapper.findById(candidate.getActivityId());
            if (activity != null && activity.getServiceId() != null) {
                candidate.setServiceId(activity.getServiceId());
            }

            // 设置默认值
            if (candidate.getTotalVotes() == null) {
                candidate.setTotalVotes(0L);
            }
            if (candidate.getSmsVotes() == null) candidate.setSmsVotes(0L);
            if (candidate.getIvrVotes() == null) candidate.setIvrVotes(0L);
            if (candidate.getWebVotes() == null) candidate.setWebVotes(0L);
            if (candidate.getAppVotes() == null) candidate.setAppVotes(0L);
            if (candidate.getWechatVotes() == null) candidate.setWechatVotes(0L);
            if (candidate.getPayVotes() == null) candidate.setPayVotes(0L);
            if (candidate.getScore() == null) candidate.setScore(java.math.BigDecimal.ZERO);
            if (candidate.getRanking() == null) candidate.setRanking(0);
            if (candidate.getStatus() == null) {
                candidate.setStatus(1); // 默认正常
            }
            if (candidate.getGroupName() == null) {
                candidate.setGroupName("1");
            }

            int result = candidateMapper.insert(candidate);
            if (result > 0) {
                // 更新活动统计
                activityMapper.updateStats(
                    candidate.getActivityId(),
                    candidateMapper.countByActivityId(candidate.getActivityId()),
                    null
                );
                return Result.success("创建成功，候选人ID: " + candidate.getId());
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            return Result.error("创建候选人失败: " + e.getMessage());
        }
    }

    /**
     * 更新候选人
     */
    @PutMapping("/{id}")
    public Result<String> updateCandidate(@PathVariable Long id, @RequestBody Candidate candidate) {
        try {
            // 检查候选人是否存在
            Candidate existing = candidateMapper.findById(id);
            if (existing == null) {
                return Result.error("候选人不存在");
            }

            // 获取活动的serviceId（前端可能不传）
            var activity = activityMapper.findById(existing.getActivityId());
            if (activity != null && activity.getServiceId() != null) {
                // 如果前端没有传serviceId，从数据库读取并保留
                candidate.setServiceId(existing.getServiceId() != null ? activity.getServiceId() : existing.getServiceId());
            } else if (existing.getServiceId() != null) {
                // 如果数据库中没有serviceId但活动有，则从活动获取
                candidate.setServiceId(activity.getServiceId());
            }

            candidate.setId(id);
            // 确保activityId不丢失
            candidate.setActivityId(existing.getActivityId());

            int result = candidateMapper.update(candidate);
            if (result > 0) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新候选人失败: " + e.getMessage());
        }
    }

    /**
     * 删除候选人（软删除）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCandidate(@PathVariable Long id) {
        try {
            // 检查候选人是否存在
            Candidate candidate = candidateMapper.findById(id);
            if (candidate == null) {
                return Result.error("候选人不存在");
            }

            int result = candidateMapper.delete(id);
            if (result > 0) {
                // 更新活动统计
                activityMapper.updateStats(
                    candidate.getActivityId(),
                    candidateMapper.countByActivityId(candidate.getActivityId()),
                    null
                );
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除候选人失败: " + e.getMessage());
        }
    }

    /**
     * 更新候选人排序
     */
    @PutMapping("/{id}/order")
    public Result<String> updateOrder(@PathVariable Long id, @RequestParam Integer orderNum) {
        try {
            Candidate candidate = candidateMapper.findById(id);
            if (candidate == null) {
                return Result.error("候选人不存在");
            }

            // 设置排序值（实际上现在用ranking字段）
            candidate.setRanking(orderNum);
            int result = candidateMapper.update(candidate);
            if (result > 0) {
                return Result.success("排序更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新排序失败: " + e.getMessage());
        }
    }

    /**
     * 批量导入候选人
     */
    @PostMapping("/batch-import")
    public Result<String> batchImport(@RequestBody CandidateBatchImportRequest request) {
        try {
            // 检查活动是否存在
            if (activityMapper.findById(request.getActivityId()) == null) {
                return Result.error("活动不存在");
            }

            // 转换导入项为候选人实体
            List<Candidate> candidates = new ArrayList<>();
            for (CandidateImportItem item : request.getCandidates()) {
                Candidate candidate = new Candidate();
                candidate.setActivityId(request.getActivityId());
                candidate.setName(item.getName());
                candidate.setDescription(item.getDescription());
                candidate.setAvatar(item.getAvatar());
                candidate.setOrderNum(item.getOrderNum() != null ? item.getOrderNum() : 0);
                candidate.setGroupName(item.getGroupName() != null ? item.getGroupName() : "1");
                candidate.setStatus(item.getStatus() != null ? item.getStatus() : 1);
                candidate.setVotes(0L);
                candidate.setTotalVotes(0L);
                candidates.add(candidate);
            }

            // 批量插入
            int count = candidateMapper.batchInsert(candidates);
            if (count > 0) {
                // 更新活动统计
                activityMapper.updateStats(
                    request.getActivityId(),
                    candidateMapper.countByActivityId(request.getActivityId()),
                    null
                );
                return Result.success("批量导入成功，共导入 " + count + " 条记录");
            } else {
                return Result.error("批量导入失败");
            }
        } catch (Exception e) {
            return Result.error("批量导入失败: " + e.getMessage());
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        return ExcelUtil.generateCandidateTemplate();
    }

    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String avatarUrl = FileUploadUtil.uploadFile(file, uploadPath);
            return Result.success(avatarUrl);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
