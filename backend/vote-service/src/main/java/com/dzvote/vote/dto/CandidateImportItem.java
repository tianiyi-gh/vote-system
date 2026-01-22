package com.dzvote.vote.dto;

import lombok.Data;

/**
 * 候选人导入项
 */
@Data
public class CandidateImportItem {
    /**
     * 候选人姓名
     */
    private String name;
    
    /**
     * 编号/排序号
     */
    private Integer orderNum;
    
    /**
     * 所属分组
     */
    private String groupName;
    
    /**
     * 简介/描述
     */
    private String description;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;
}
