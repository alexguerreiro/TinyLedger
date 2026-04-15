package com.teya.tinyledger.dto;

import java.time.LocalDateTime;

public class TransactionResponse {
    private String message;
    private Integer statusCode;
    private LocalDateTime timestamp;
    private Object data;

    public TransactionResponse(String message, Integer statusCode, Object data) {
        this.message = message;
        this.statusCode = statusCode;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Object getData() {
        return data;
    }
}

