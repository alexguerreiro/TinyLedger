package com.teya.tinyledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        description = "Response object containing account balance information",
        example = "{\"accountId\": \"550e8400-e29b-41d4-a716-446655440000\", \"accountName\": \"John Doe\", \"balance\": 1000.0}"
)
public class BalanceResponse {
    @Schema(description = "The unique identifier of the account", example = "550e8400-e29b-41d4-a716-446655440000")
    private String accountId;

    @Schema(description = "The name of the account owner", example = "John Doe")
    private String accountName;

    @Schema(description = "The current balance of the account", example = "1000.0")
    private BigDecimal balance;

    public BalanceResponse(String accountId, String accountName, BigDecimal balance) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.balance = balance;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}

