package com.dzvote.vote.dto;

import lombok.Data;
import java.util.List;

/**
 * 候选人批量导入请求
 */
@Data
public class CandidateBatchImportRequest {
    /**
     * 活动ID
     */
    private Long activityId;
    
    /**
     * 候选人列表
     */
    private List<CandidateImportItem> candidates;
}
