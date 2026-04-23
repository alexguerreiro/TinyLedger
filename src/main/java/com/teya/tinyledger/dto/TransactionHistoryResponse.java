package com.teya.tinyledger.dto;

import com.teya.tinyledger.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class TransactionHistoryResponse {
    private String accountId;
    private String accountName;
    private BigDecimal currentBalance;
    private List<Transaction> transactions;
    private Integer transactionCount;

    public TransactionHistoryResponse(String accountId, String accountName, BigDecimal currentBalance, List<Transaction> transactions) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.currentBalance = currentBalance;
        this.transactions = transactions;
        this.transactionCount = transactions != null ? transactions.size() : 0;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }
}

