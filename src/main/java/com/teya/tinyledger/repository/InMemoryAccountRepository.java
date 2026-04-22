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
     * Atomically updates the account stored for the given ID when that account exists.
     * The update is performed as a single per-key operation on the backing {@link ConcurrentHashMap},
     * so concurrent updates to the same account are serialized while updates to different accounts
     * may still proceed independently.
     * <p>
     * If the account is not present, no update is applied and {@code null} is returned.
     * If the update function throws any (hypothetical) exception, it is logged and wrapped in a
     * {@link DatabaseUpdateException} to preserve the repository's retryable failure contract.
     *
     * @param accountId the account ID to update
     * @param updateFunction function that receives the current account value and returns the replacement value
     * @return the updated account, or {@code null} if no account exists for the given ID
     * @throws DatabaseUpdateException if the update function fails during the atomic update
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
