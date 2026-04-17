package com.teya.tinyledger.repository;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.exception.DatabaseUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.UnaryOperator;

@Component
public class AccountRepo {

    private static final Logger logger = LoggerFactory.getLogger(AccountRepo.class);

    // simulates an account database
    private final Map<String, Account> accountsDB = new ConcurrentHashMap<>();

    // Per-account locks for fine-grained concurrency control
    private final Map<String, ReentrantReadWriteLock> accountLocks = new ConcurrentHashMap<>();

    public void createAccount(String accountId, Account account) {
        Account existentAccount = getAccount(accountId);

        if (existentAccount != null) {
            throw new IllegalArgumentException("Account with id " + accountId + " already exists");
        }

        accountsDB.put(accountId, account);
    }

    public Account getAccount(String accountId) {
        ReentrantReadWriteLock lock = accountLocks.computeIfAbsent(accountId, id -> new ReentrantReadWriteLock());
        lock.readLock().lock();
        try {
            return accountsDB.get(accountId);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Atomically updates an account using per-account write locks.
     * This ensures thread-safe updates while allowing concurrent updates to different accounts.
     * <p>
     * If a (hypothetical) database error occurs during the update operation, a DatabaseUpdateException is thrown
     * to signal a retryable failure. This typically represents transient database issues like
     * connection timeouts or temporary unavailability.
     *
     * @param accountId      the account ID
     * @param updateFunction function to apply to the account
     * @return the updated account, or null if account doesn't exist
     * @throws DatabaseUpdateException if a database update operation fails (retryable)
     */
    public Account updateAccountAtomically(String accountId, UnaryOperator<Account> updateFunction) {
        ReentrantReadWriteLock lock = accountLocks.computeIfAbsent(accountId, id -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            Account currentAccount = accountsDB.get(accountId);
            if (currentAccount == null) {
                logger.error("Account not found for id: {}", accountId);
                return null;
            }

            // Apply the update function to create a new state
            Account updatedAccount = updateFunction.apply(currentAccount);
            // Update the in-memory db
            accountsDB.put(accountId, updatedAccount);

            return updatedAccount;
        } catch (Exception e) {
            logger.error("Unexpected error during account update for id: {}", accountId, e);
            throw new DatabaseUpdateException("Database update failed for account: " + accountId, e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}

