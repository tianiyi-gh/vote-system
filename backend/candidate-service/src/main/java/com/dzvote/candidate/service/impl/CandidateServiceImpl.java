package com.dzvote.candidate.service.impl;

import com.dzvote.candidate.entity.Candidate;
import com.dzvote.candidate.mapper.CandidateMapper;
import com.dzvote.candidate.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 候选人服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {
    
    private final CandidateMapper candidateMapper;
    
    @Override
    public List<Candidate> listCandidates(Long activityId, Integer page, Integer size) {
        try {
            int offset = (page != null && page > 0 ? (page - 1) * size : 0);
            int limit = size != null && size > 0 ? size : 10;
            List<Candidate> candidates = candidateMapper.selectList(activityId, offset, limit);
            log.info("查询候选人列表成功，活动ID={}, 数量={}", activityId, candidates.size());
            return candidates;
        } catch (Exception e) {
            log.error("获取候选人列表失败", e);
            throw new RuntimeException("获取候选人列表失败", e);
        }
    }
    
    @Override
    public Candidate getCandidate(Long id) {
        try {
            Candidate candidate = candidateMapper.selectById(id);
            if (candidate != null) {
                log.info("查询候选人详情成功: ID={}", id);
            } else {
                log.warn("候选人不存在: ID={}", id);
            }
            return candidate;
        } catch (Exception e) {
            log.error("获取候选人详情失败", e);
            throw new RuntimeException("获取候选人详情失败", e);
        }
    }
    
    @Override
    public Candidate createCandidate(Candidate candidate) {
        try {
            candidate.setCreatedAt(LocalDateTime.now());
            candidate.setUpdatedAt(LocalDateTime.now());
            if (candidate.getStatus() == null) {
                candidate.setStatus(1);
            }
            if (candidate.getVotes() == null) {
                candidate.setVotes(0);
            }
            if (candidate.getOrderNum() == null) {
                candidate.setOrderNum(0);
            }
            
            // 如果前端没有传递serviceId，从活动表中获取
            if (candidate.getServiceId() == null && candidate.getActivityId() != null) {
                String serviceId = candidateMapper.selectServiceIdByActivityId(candidate.getActivityId());
                if (serviceId != null) {
                    candidate.setServiceId(serviceId);
                    log.info("从活动中获取serviceId: activityId={}, serviceId={}", 
                             candidate.getActivityId(), serviceId);
                }
            }
            
            int result = candidateMapper.insert(candidate);
            if (result > 0) {
                log.info("创建候选人成功: ID={}, 姓名={}, candidateNo={}, serviceId={}", 
                         candidate.getId(), candidate.getName(), candidate.getCandidateNo(), candidate.getServiceId());
                return candidate;
            } else {
                throw new RuntimeException("创建候选人失败");
            }
        } catch (Exception e) {
            log.error("创建候选人失败", e);
            throw new RuntimeException("创建候选人失败", e);
        }
    }
    
    @Override
    public Candidate updateCandidate(Candidate candidate) {
        try {
            candidate.setUpdatedAt(LocalDateTime.now());
            int result = candidateMapper.updateById(candidate);
            if (result > 0) {
                log.info("更新候选人成功: ID={}, 姓名={}, candidateNo={}, serviceId={}", 
                         candidate.getId(), candidate.getName(), candidate.getCandidateNo(), candidate.getServiceId());
                return candidateMapper.selectById(candidate.getId());
            } else {
                log.warn("候选人不存在或更新失败: ID={}", candidate.getId());
                return null;
            }
        } catch (Exception e) {
            log.error("更新候选人失败", e);
            throw new RuntimeException("更新候选人失败", e);
        }
    }
    
    @Override
    public boolean deleteCandidate(Long id) {
        try {
            int result = candidateMapper.deleteById(id);
            if (result > 0) {
                log.info("删除候选人成功: ID={}", id);
                return true;
            } else {
                log.warn("候选人不存在或删除失败: ID={}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("删除候选人失败", e);
            throw new RuntimeException("删除候选人失败", e);
        }
    }
}
