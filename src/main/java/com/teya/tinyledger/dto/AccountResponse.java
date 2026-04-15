package com.teya.tinyledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
        description = "Response object returned when creating or retrieving an account",
        example = "{\"accountId\": \"550e8400-e29b-41d4-a716-446655440000\", \"accountName\": \"John Doe\", \"balance\": 1000.0, \"message\": \"Account created successfully\", \"statusCode\": 201}"
)
public class AccountResponse {
    @Schema(description = "The unique identifier (UUID) of the account", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID accountId;

    @Schema(description = "The name of the account owner", example = "John Doe")
    private String accountName;

    @Schema(description = "The current balance of the account", example = "1000.0")
    private Double balance;

    @Schema(description = "Status or error message", example = "Account created successfully")
    private String message;

    @Schema(description = "HTTP status code", example = "201")
    private Integer statusCode;

    public AccountResponse(UUID accountId, String accountName, Double balance, String message, Integer statusCode) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.balance = balance;
        this.message = message;
        this.statusCode = statusCode;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public Double getBalance() {
        return balance;
    }

    public String getMessage() {
        return message;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}

