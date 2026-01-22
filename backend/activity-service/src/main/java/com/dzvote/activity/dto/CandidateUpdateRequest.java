package com.dzvote.activity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 候选人更新请求DTO
 */
@Data
public class CandidateUpdateRequest {

    /**
     * 候选人姓名
     */
    @NotBlank(message = "候选人姓名不能为空")
    private String name;

    /**
     * 所属分组
     */
    private String groupName;

    /**
     * 简介
     */
    private String description;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 状态：0-正常 1-禁用
     */
    private Integer status;
}