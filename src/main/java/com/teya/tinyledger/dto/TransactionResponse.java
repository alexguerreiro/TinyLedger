package com.teya.tinyledger.dto;

public class TransactionResponse {
    private String message;
    private Integer statusCode;

    public TransactionResponse(String message, Integer statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}

