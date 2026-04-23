package com.teya.tinyledger.domain;

import com.teya.tinyledger.dto.TransactionRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a failed transaction that will be retried.
 * Tracks the number of retry attempts and the last failure reason.
 */
public class FailedTransaction implements Serializable {
    private final String accountId;
    private final TransactionRequest transactionRequest;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastRetryAt;

    public FailedTransaction(String accountId, TransactionRequest transactionRequest) {
        this.accountId = accountId;
        this.transactionRequest = transactionRequest;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.lastRetryAt = LocalDateTime.now();
    }

    public String getAccountId() {
        return accountId;
    }

    public TransactionRequest getTransactionRequest() {
        return transactionRequest;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }
}
