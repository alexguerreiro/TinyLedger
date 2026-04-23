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
     * Creates a new Account with the given transaction applied to both the balance and transaction history.
     *
     * @param transaction the transaction to apply
     * @return a new Account instance reflecting the applied transaction
     */
    public Account applyTransaction(Transaction transaction) {
        Set<Transaction> transactionsCopy = new HashSet<>(this.transactions);
        transactionsCopy.add(transaction);

        BigDecimal newBalance = transaction.transactionType() == TransactionType.DEPOSIT
                ? this.balance.add(transaction.amount())
                : this.balance.subtract(transaction.amount());

        return new Account(this.id, this.name, newBalance, transactionsCopy);
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