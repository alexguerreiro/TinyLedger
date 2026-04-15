package com.teya.tinyledger.repository;

import com.teya.tinyledger.domain.Account;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@Component
public class AccountRepo {

    // simulates an account database
    private Map<UUID, Account> accountsDB = new ConcurrentHashMap<>();

    public Account getAccount(UUID accountId) {
        return accountsDB.get(accountId);
    }

    public void saveAccount(UUID accountId, Account account) {
        accountsDB.put(accountId, account);
    }


    /**
     * Atomically updates an account using a compare-and-swap approach.
     * This prevents lost updates due to race conditions.
     *
     * @param accountId the account ID
     * @param updateFunction function to apply to the account
     * @return the updated account, or null if account doesn't exist
     * @throws IllegalStateException if unable to update after retries
     */
    public Account updateAccountAtomically(UUID accountId, UnaryOperator<Account> updateFunction) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            Account currentAccount = accountsDB.get(accountId);

            if (currentAccount == null) {
                return null;
            }

            // Apply the update function to create a new state
            Account updatedAccount = updateFunction.apply(currentAccount);

            // Try to atomically replace the old account with the updated one
            if (accountsDB.replace(accountId, currentAccount, updatedAccount)) {
                return updatedAccount;
            }

            retryCount++;
        }

        throw new IllegalStateException("Failed to update account " + accountId + " after " + maxRetries + " retries");
    }
}

