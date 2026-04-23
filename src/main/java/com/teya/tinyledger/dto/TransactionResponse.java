package com.teya.tinyledger.dto;

import com.teya.tinyledger.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private LocalDateTime createdAt;

    public TransactionResponse(String transactionId, String accountId, BigDecimal amount,
                               TransactionType transactionType, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.createdAt = createdAt;
    }

    public String getTransactionId() {
        return transactionId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
