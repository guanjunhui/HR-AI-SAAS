package com.hrai.business.controller;

import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.employmentevent.EmploymentEventApproveRequest;
import com.hrai.business.dto.employmentevent.EmploymentEventCreateRequest;
import com.hrai.business.dto.employmentevent.EmploymentEventDetailResponse;
import com.hrai.business.dto.employmentevent.EmploymentEventQueryRequest;
import com.hrai.business.service.EmploymentEventService;
import com.hrai.common.dto.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import com.alibaba.excel.EasyExcel;
import com.hrai.business.dto.employmentevent.EmploymentEventImportDTO;
import com.hrai.business.dto.employmentevent.EmploymentEventStatisticsDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 任职事件管理 Controller
 */
@RestController
@RequestMapping("/api/v1/hr/employment-events")
public class EmploymentEventController {

    private final EmploymentEventService eventService;

    public EmploymentEventController(EmploymentEventService eventService) {
        this.eventService = eventService;
    }

    /**
     * 分页查询任职事件列表
     */
    @GetMapping
    public Result<PageResponse<EmploymentEventDetailResponse>> list(EmploymentEventQueryRequest query) {
        return Result.success(eventService.list(query));
    }

    /**
     * 根据ID获取任职事件详情
     */
    @GetMapping("/{id}")
    public Result<EmploymentEventDetailResponse> getById(@PathVariable("id") Long id) {
        return Result.success(eventService.getById(id));
    }

    /**
     * 创建任职事件（草稿状态）
     */
    @PostMapping
    public Result<Long> create(@RequestBody @Valid EmploymentEventCreateRequest request) {
        return Result.success(eventService.create(request));
    }

    /**
     * 提交审批
     */
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable("id") Long id) {
        eventService.submit(id);
        return Result.success();
    }

    /**
     * 审批任职事件
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable("id") Long id, @RequestBody EmploymentEventApproveRequest request) {
        eventService.approve(id, request);
        return Result.success();
    }

    /**
     * 取消任职事件
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        eventService.cancel(id);
        return Result.success();
    }

    /**
     * 删除任职事件（仅草稿状态可删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        eventService.delete(id);
        return Result.success();
    }

    /**
     * 批量导入任职事件
     */
    @PostMapping("/import")
    public Result<Void> importEvents(@RequestParam("file") MultipartFile file) {
        eventService.importEvents(file);
        return Result.success();
    }

    /**
     * 下载导入模版
     */
    @GetMapping("/import/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("任职事件导入模版", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream(), EmploymentEventImportDTO.class)
                .sheet("模版")
                .doWrite(Collections.emptyList());
    }

    /**
     * 获取任职事件统计数据
     */
    @GetMapping("/statistics")
    public Result<EmploymentEventStatisticsDTO> getStatistics() {
        return Result.success(eventService.getStatistics());
    }
}
