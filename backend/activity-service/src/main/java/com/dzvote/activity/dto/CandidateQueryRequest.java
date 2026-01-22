package com.dzvote.activity.dto;

import lombok.Data;

/**
 * 候选人查询请求DTO
 */
@Data
public class CandidateQueryRequest {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 候选人姓名（模糊查询）
     */
    private String name;

    /**
     * 所属分组
     */
    private String groupName;

    /**
     * 状态：0-正常 1-禁用
     */
    private Integer status;

    /**
     * 排序字段：votes（票数）、score（得分）、name（姓名）
     */
    private String orderBy = "votes";

    /**
     * 排序方向：asc、desc
     */
    private String orderDirection = "desc";
}