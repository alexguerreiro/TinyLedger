package com.teya.tinyledger.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Account {
    private UUID id;
    private String name;
    private double balance;
    private Set<Transaction> transactions;
    private static final Logger logger = LoggerFactory.getLogger(Account.class);

    public Account(String name, Double balance) {
        if (balance < 0) {
            logger.error("Account balance cannot be negative: {}", balance);
            throw new IllegalArgumentException("Account balance cannot be negative");
        }
        this.id = UUID.randomUUID();
        this.name = name;
        this.balance = balance;
        this.transactions = new HashSet<>();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
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