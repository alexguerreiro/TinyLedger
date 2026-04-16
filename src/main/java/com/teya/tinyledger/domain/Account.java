package com.teya.tinyledger.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

public class Account {
    private String id;
    private String name;
    private BigDecimal balance;
    private Set<Transaction> transactions;
    private static final Logger logger = LoggerFactory.getLogger(Account.class);

    public Account(String name, BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            logger.error("Account balance cannot be negative: {}", balance);
            throw new IllegalArgumentException("Account balance cannot be negative");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.balance = balance;
        this.transactions = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Set<Transaction> getTransactions() {
        return Collections.unmodifiableSet(transactions);
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}