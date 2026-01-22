package com.dzvote.candidate.service;

import com.dzvote.candidate.entity.Candidate;

import java.util.List;

/**
 * 候选人服务接口
 */
public interface CandidateService {
    
    /**
     * 获取候选人列表
     */
    List<Candidate> listCandidates(Long activityId, Integer page, Integer size);
    
    /**
     * 获取候选人详情
     */
    Candidate getCandidate(Long id);
    
    /**
     * 创建候选人
     */
    Candidate createCandidate(Candidate candidate);
    
    /**
     * 更新候选人
     */
    Candidate updateCandidate(Candidate candidate);
    
    /**
     * 删除候选人
     */
    boolean deleteCandidate(Long id);
}
