package com.teya.tinyledger.dto;

import com.teya.tinyledger.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(
        description = "Request object for transaction operations (deposit or withdrawal)",
        example = "{\"accountId\": \"550e8400-e29b-41d4-a716-446655440000\", \"amount\": 500.0, \"transactionType\": \"DEPOSIT\"}"
)
public class TransactionRequest {

    @Schema(
            description = "The unique identifier of the account",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Account ID is mandatory and cannot be null")
    private String accountId;

    @Schema(
            description = "The transaction amount (must be greater than zero)",
            example = "500.0",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01"
    )
    @NotNull(message = "Transaction amount is mandatory and cannot be null")
    @DecimalMin(value = "0.01", message = "Transaction amount must be greater than zero")
    private BigDecimal amount;

    @Schema(
            description = "The TransactionType is mandatory and cannot be null",
            example = "DEPOSIT",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = "DEPOSIT, WITHDRAWAL"
    )
    @NotNull(message = "TransactionType is mandatory and cannot be null")
    private TransactionType transactionType;

    public TransactionRequest(String accountId, BigDecimal amount, TransactionType type) {
        this.accountId = accountId;
        this.amount = amount;
        this.transactionType = type;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }
}


