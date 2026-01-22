package com.dzvote.activity.util;

import lombok.Data;

import java.util.List;

/**
 * 简化分页结果类
 */
@Data
public class PageResultSimple<T> {
    private Long total;
    private List<T> records;
    private Long current;
    private Long size;
    private Long pages;

    public PageResultSimple() {}

    public PageResultSimple(Long total, List<T> records, Long current, Long size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size;
    }
}