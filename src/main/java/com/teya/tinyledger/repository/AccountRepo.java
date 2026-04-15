package com.teya.tinyledger.repository;

import com.teya.tinyledger.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.UnaryOperator;

@Component
public class AccountRepo {

    private static final Logger logger = LoggerFactory.getLogger(AccountRepo.class);

    // simulates an account database
    private final Map<UUID, Account> accountsDB = new ConcurrentHashMap<>();

    // Per-account locks for fine-grained concurrency control
    private final Map<UUID, ReentrantReadWriteLock> accountLocks = new ConcurrentHashMap<>();

    public Account getAccount(UUID accountId) {
        ReentrantReadWriteLock lock = accountLocks.computeIfAbsent(accountId, id -> new ReentrantReadWriteLock());
        lock.readLock().lock();
        try {
            return accountsDB.get(accountId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void saveAccount(UUID accountId, Account account) {
        ReentrantReadWriteLock lock = accountLocks.computeIfAbsent(accountId, id -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            accountsDB.put(accountId, account);
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Atomically updates an account using per-account write locks.
     * This ensures thread-safe updates while allowing concurrent updates to different accounts.
     *
     * @param accountId the account ID
     * @param updateFunction function to apply to the account
     * @return the updated account, or null if account doesn't exist
     */
    public Account updateAccountAtomically(UUID accountId, UnaryOperator<Account> updateFunction) {
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

            // Update the account in the database
            accountsDB.put(accountId, updatedAccount);

            return updatedAccount;
        } finally {
            lock.writeLock().unlock();
        }
    }
}

