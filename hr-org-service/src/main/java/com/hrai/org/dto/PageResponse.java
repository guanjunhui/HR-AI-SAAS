package com.hrai.org.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 分页响应
 */
public class PageResponse<T> {

    private long pageNo;
    private long pageSize;
    private long total;
    private List<T> records;

    public long getPageNo() { return pageNo; }
    public void setPageNo(long pageNo) { this.pageNo = pageNo; }

    public long getPageSize() { return pageSize; }
    public void setPageSize(long pageSize) { this.pageSize = pageSize; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }

    public static <T> PageResponse<T> from(IPage<?> page, List<T> records) {
        PageResponse<T> response = new PageResponse<>();
        response.setPageNo(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        response.setRecords(records);
        return response;
    }
}
