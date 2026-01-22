package com.dzvote.vote.dto;

import lombok.Data;

/**
 * 报表导出请求
 */
@Data
public class ReportExportRequest {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 导出类型：EXCEL/CSV
     */
    private String exportType;

    /**
     * 报表类型：VOTE_RECORDS/CANDIDATE_STATS/ACTIVITY_SUMMARY
     */
    private String reportType;

    /**
     * 开始日期（格式：yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 结束日期（格式：yyyy-MM-dd）
     */
    private String endDate;
}
