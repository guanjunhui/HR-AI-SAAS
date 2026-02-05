package com.hrai.business.dto;

import java.util.List;

/**
 * 分页响应
 */
public class PageResponse<T> {

    private List<T> records;
    private Long total;
    private Integer pageNo;
    private Integer pageSize;
    private Integer totalPages;

    public PageResponse() {}

    public PageResponse(List<T> records, Long total, Integer pageNo, Integer pageSize) {
        this.records = records;
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    public static <T> PageResponse<T> of(List<T> records, Long total, Integer pageNo, Integer pageSize) {
        return new PageResponse<>(records, total, pageNo, pageSize);
    }

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }

    public Integer getPageNo() { return pageNo; }
    public void setPageNo(Integer pageNo) { this.pageNo = pageNo; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
}
