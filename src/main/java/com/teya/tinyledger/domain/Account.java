package com.teya.tinyledger.domain;

import com.teya.tinyledger.exception.InvalidTransactionException;
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
     * Adds transaction and updates the account balance
     *
     * @param transaction the transaction to add
     * @return a new Account instance reflecting the applied transaction
     */
    public Account addTransaction(Transaction transaction) {
        if (this.transactions.contains(transaction)) {
            logger.error("Duplicate transaction detected for account {}: {}", this.id, transaction.id());
            throw new InvalidTransactionException("Duplicate transaction id: " + transaction.id());
        }

        Set<Transaction> transactionsCopy = new HashSet<>(this.transactions);
        transactionsCopy.add(transaction);

        BigDecimal newBalance = transaction.transactionType() == TransactionType.DEPOSIT
                ? this.balance.add(transaction.amount())
                : this.balance.subtract(transaction.amount());

        return new Account(this.id, this.name, newBalance, transactionsCopy);
    }

    public Account rollbackTransaction(Transaction transaction) {
        if (!this.transactions.contains(transaction)) {
            logger.error("Transaction does not exist {}: {}", this.id, transaction.id());
            throw new InvalidTransactionException("Transaction does not exist for id: " + transaction.id());
        }

        Set<Transaction> transactionsCopy = new HashSet<>(this.transactions);

        transactionsCopy.remove(transaction);

        BigDecimal newBalance = transaction.transactionType() == TransactionType.DEPOSIT
                ? this.balance.subtract(transaction.amount())
                : this.balance.add(transaction.amount());

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