package com.teya.tinyledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

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
    @NotNull(message = "Initial balance is mandatory and cannot be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance cannot be negative")
    private BigDecimal initialBalance;

    public AccountRequest(String accountName, BigDecimal initialBalance) {
        this.accountName = accountName;
        this.initialBalance = initialBalance;
    }

    public String getAccountName() {
        return accountName;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }
}





