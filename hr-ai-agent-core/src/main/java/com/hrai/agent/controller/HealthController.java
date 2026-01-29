package com.hrai.agent.controller;

import com.hrai.common.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 *
 * @author HR AI Team
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "hr-ai-agent-core");
        data.put("timestamp", LocalDateTime.now());
        data.put("message", "Agent核心服务运行正常");

        return Result.success(data);
    }

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}
