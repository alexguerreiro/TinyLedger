package com.teya.tinyledger.dto;

import com.teya.tinyledger.domain.Transaction;

import java.util.Set;
import java.util.UUID;

public class TransactionHistoryResponse {
    private UUID accountId;
    private String accountName;
    private Double currentBalance;
    private Set<Transaction> transactions;
    private Integer transactionCount;

    public TransactionHistoryResponse(UUID accountId, String accountName, Double currentBalance, Set<Transaction> transactions) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.currentBalance = currentBalance;
        this.transactions = transactions;
        this.transactionCount = transactions != null ? transactions.size() : 0;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public Double getCurrentBalance() {
        return currentBalance;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }
}

