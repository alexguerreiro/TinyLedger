package com.teya.tinyledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        description = "Request object for creating a new account",
        example = "{\"accountName\": \"John Doe\", \"initialBalance\": 1000.0}"
)
public class AccountRequest {

    @Schema(
            description = "The name of the account owner",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Account name is mandatory and cannot be null or empty")
    private String accountName;

    @Schema(
            description = "The initial balance of the account in dollars",
            example = "1000.0",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.0"
    )
    private double initialBalance;

    public AccountRequest(String accountName, double initialBalance) {
        this.accountName = accountName;
        this.initialBalance = initialBalance;
    }

    public String getAccountName() {
        return accountName;
    }

    public Double getInitialBalance() {
        return initialBalance;
    }
}



