package com.teya.tinyledger.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

public final class Account {
    private final String id;
    private final String name;
    private final BigDecimal balance;
    private final Set<Transaction> transactions;
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

    // Private constructor for creating updated copies
    private Account(String id, String name, BigDecimal balance, Set<Transaction> transactions) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.transactions = transactions;
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

    public Set<Transaction> getTransactions() {
        return Collections.unmodifiableSet(transactions);
    }

    /**
     * Creates a new Account with updated balance (immutable pattern).
     *
     * @param newBalance the new balance
     * @return a new Account instance with the updated balance
     */
    public Account withBalance(BigDecimal newBalance) {
        Set<Transaction> transactionsCopy = new HashSet<>(this.transactions);
        return new Account(this.id, this.name, newBalance, transactionsCopy);
    }

    /**
     * Creates a new Account with an additional transaction (immutable pattern).
     *
     * @param transaction the transaction to add
     * @return a new Account instance with the additional transaction
     */
    public Account withTransaction(Transaction transaction) {
        Set<Transaction> transactionsCopy = new HashSet<>(this.transactions);
        transactionsCopy.add(transaction);
        return new Account(this.id, this.name, this.balance, transactionsCopy);
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