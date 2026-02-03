package com.hrai.common.dto;

import java.io.Serializable;

/**
 * 统一响应结果封装
 *
 * @author HR AI Team
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应码 */
    private Integer code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 请求ID (用于链路追踪) */
    private String requestId;

    /** 时间戳 */
    private Long timestamp;

    public Result() {}

    public Result(Integer code, String message, T data, String requestId, Long timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
        this.timestamp = timestamp;
    }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "success";
        result.data = data;
        result.timestamp = System.currentTimeMillis();
        return result;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.timestamp = System.currentTimeMillis();
        return result;
    }

    public Result<T> requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }
}
