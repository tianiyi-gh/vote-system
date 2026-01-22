package com.dzvote.activity.dto;

import lombok.Data;

/**
 * 活动查询请求
 */
@Data
public class ActivityQueryRequest {
    
    /**
     * 页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer size;
    
    /**
     * 活动状态 (0:未开始, 1:进行中, 2:已结束)
     */
    private Integer status;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 搜索关键词
     */
    private String keyword;
}
