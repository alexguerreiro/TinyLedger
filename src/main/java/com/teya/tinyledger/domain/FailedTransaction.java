package com.teya.tinyledger.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a failed transaction that will be retried.
 * Tracks the number of retry attempts and the last failure reason.
 */
public class FailedTransaction implements Serializable {
    private final String accountId;
    private final Transaction transaction;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastRetryAt;

    public FailedTransaction(String accountId, Transaction transaction) {
        this.accountId = accountId;
        this.transaction = transaction;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.lastRetryAt = LocalDateTime.now();
    }

    public String getAccountId() {
        return accountId;
    }

    public Transaction getTransaction() {
        return transaction;
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
