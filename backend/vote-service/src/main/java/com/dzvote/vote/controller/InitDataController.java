package com.dzvote.vote.controller;

import com.dzvote.vote.entity.Activity;
import com.dzvote.vote.entity.Candidate;
import com.dzvote.vote.mapper.ActivityMapper;
import com.dzvote.vote.mapper.CandidateMapper;
import com.dzvote.vote.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 初始化测试数据控制器
 */
@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
public class InitDataController {

    private final ActivityMapper activityMapper;
    private final CandidateMapper candidateMapper;

    /**
     * 初始化测试数据
     */
    @PostMapping("/testdata")
    public Result<String> initTestData() {
        try {
            // 1. 创建测试活动
            Activity activity = new Activity();
            activity.setTitle("2024年度优秀员工评选");
            activity.setDescription("评选公司年度优秀员工，每人可投3票，公平公正公开");
            activity.setStartTime(LocalDateTime.of(2024, 1, 1, 9, 0));
            activity.setEndTime(LocalDateTime.of(2025, 12, 31, 18, 0));
            activity.setStatus(1); // 进行中
            activity.setVoteLimit(3);
            activity.setCreateBy("admin");
            activity.setCreateTime(LocalDateTime.now());
            activity.setDeleted(0);

            activityMapper.insert(activity);
            System.out.println("创建活动成功，ID: " + activity.getId());

            // 2. 创建候选人
            String[] candidateNames = {"张三", "李四", "王五", "赵六"};
            String[] candidateDescs = {
                "技术部高级工程师，工作认真负责，技术能力强",
                "销售部销售经理，业绩突出，团队协作好",
                "市场部产品经理，创新能力突出，执行力强",
                "人事部主管，管理能力强，团队建设好"
            };
            String[] colors = {"4CAF50", "2196F3", "FF9800", "E91E63"};

            for (int i = 0; i < candidateNames.length; i++) {
                Candidate candidate = new Candidate();
                candidate.setActivityId(activity.getId());
                candidate.setName(candidateNames[i]);
                candidate.setDescription(candidateDescs[i]);
                candidate.setAvatar("https://via.placeholder.com/150/" + colors[i] + "/ffffff?text=" + candidateNames[i]);
                candidate.setVotes((long) (i + 1) * 5); // 5, 10, 15, 20
                candidate.setOrderNum(i + 1);
                candidate.setStatus(1); // 启用
                candidate.setCreatedAt(LocalDateTime.now());

                candidateMapper.insert(candidate);
                System.out.println("创建候选人成功: " + candidateNames[i]);
            }

            // 3. 更新活动统计
            activityMapper.updateStats(activity.getId(), candidateNames.length, 50);

            return Result.success("测试数据初始化成功！活动ID: " + activity.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("初始化测试数据失败: " + e.getMessage());
        }
    }
}
