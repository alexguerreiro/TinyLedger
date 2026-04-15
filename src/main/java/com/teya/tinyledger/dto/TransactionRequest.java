package com.teya.tinyledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Schema(
        description = "Request object for transaction operations (deposit or withdrawal)",
        example = "{\"accountId\": \"550e8400-e29b-41d4-a716-446655440000\", \"amount\": 500.0}"
)
public class TransactionRequest {

    @Schema(
            description = "The unique identifier (UUID) of the account",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Account ID is mandatory and cannot be null")
    private UUID accountId;

    @Schema(
            description = "The transaction amount in dollars",
            example = "500.0",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01"
    )
    @NotNull(message = "Transaction amount is mandatory and cannot be null")
    @Positive(message = "Transaction amount must be greater than zero")
    private Double amount;

    public TransactionRequest(UUID accountId, Double amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Double getAmount() {
        return amount;
    }

}
