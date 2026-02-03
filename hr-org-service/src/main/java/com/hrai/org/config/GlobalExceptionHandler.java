package com.hrai.org.config;

import com.hrai.common.dto.Result;
import com.hrai.common.exception.BizException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * 将 BizException 等异常统一转换为 Result<T> 格式响应，
 * BizException 返回 HTTP 200（业务错误码在 Result.code 中），
 * 避免与网关 JWT 401 冲突。
 *
 * 通过 HttpServletResponse 显式设置 Content-Type，
 * 防止因客户端 Accept 头导致内容协商失败。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常 - 返回 HTTP 200，错误码放在 Result.code 中
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e, HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常（@Valid 触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e, HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("参数校验异常: {}", message);
        return Result.fail(400, message);
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        log.error("未预期异常: {}", e.getMessage(), e);
        return Result.fail(500, "服务内部错误");
    }
}
