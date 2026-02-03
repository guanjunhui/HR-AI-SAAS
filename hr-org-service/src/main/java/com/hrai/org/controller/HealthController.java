package com.hrai.org.controller;

import com.hrai.common.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "hr-org-service");
        data.put("timestamp", System.currentTimeMillis());
        return Result.success(data);
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
