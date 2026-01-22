package com.dzvote.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 候选人创建请求DTO
 */
@Data
public class CandidateCreateRequest {

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    /**
     * 候选人编号
     */
    private String candidateNo;

    /**
     * 候选人姓名
     */
    @NotBlank(message = "候选人姓名不能为空")
    private String name;

    /**
     * 所属分组
     */
    private String groupName = "1";

    /**
     * 简介
     */
    private String description;

    /**
     * 头像URL
     */
    private String avatar;
}