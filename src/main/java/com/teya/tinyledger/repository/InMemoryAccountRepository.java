package com.teya.tinyledger.repository;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.exception.DatabaseUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@Component
public class InMemoryAccountRepository implements AccountRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryAccountRepository.class);

    // simulates an account database
    private final Map<String, Account> accountsDB = new ConcurrentHashMap<>();

    @Override
    public void createAccount(String accountId, Account account) {
        Account existentAccount = getAccount(accountId);

        if (existentAccount != null) {
            throw new IllegalArgumentException("Account with id " + accountId + " already exists");
        }

        accountsDB.put(accountId, account);
    }

    @Override
    public Account getAccount(String accountId) {
        return accountsDB.get(accountId);
    }

    /**
     * Atomically updates an existing account.
     *
     * @param accountId the account ID to update
     * @param updateFunction function applied to the current account value
     * @return the updated account, or {@code null} if the account does not exist
     * @throws DatabaseUpdateException if the update fails (in a hypothetical scenario)
     */
    @Override
    public Account updateAccount(String accountId, UnaryOperator<Account> updateFunction) {
        try {
            return accountsDB.computeIfPresent(
                    accountId, (id, currentAccount) -> updateFunction.apply(currentAccount));
        } catch (Exception e) {
            logger.error("Unexpected error during account update for id: {}", accountId, e);
            throw new DatabaseUpdateException("Database update failed for account: " + accountId, e);
        }
    }
}
