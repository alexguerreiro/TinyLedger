package com.teya.tinyledger.repository;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.Transaction;
import com.teya.tinyledger.domain.TransactionType;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.exception.DatabaseUpdateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryAccountRepo Unit Tests")
class InMemoryAccountRepositoryTest {

    private InMemoryAccountRepository accountRepo;

    @BeforeEach
    void setUp() {
        accountRepo = new InMemoryAccountRepository();
    }

    @Test
    @DisplayName("Should create and retrieve an account")
    void createAccountShouldStoreAccount() {
        Account account = new Account("Test", new BigDecimal("100.0"));

        accountRepo.createAccount(account.getId(), account);

        Account storedAccount = accountRepo.getAccount(account.getId());
        assertNotNull(storedAccount);
        assertSame(account, storedAccount);
    }

    @Test
    @DisplayName("Should reject duplicate account IDs")
    void createAccountShouldRejectDuplicateIds() {
        Account account = new Account("Test", new BigDecimal("100.0"));
        accountRepo.createAccount(account.getId(), account);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountRepo.createAccount(account.getId(), account));

        assertEquals("Account with id " + account.getId() + " already exists", exception.getMessage());
    }

    @Test
    @DisplayName("Should Throw AccountNotFoundException")
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        String nonExistentAccountId = "acc-123";

        UnaryOperator<Account> updateFunction = acc ->
                new Account("Test", new BigDecimal("100.0"));;

        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> accountRepo.updateAccount(nonExistentAccountId, updateFunction)
        );

        assertEquals("Account not found: " + nonExistentAccountId, exception.getMessage());
    }

    @Test
    @DisplayName("Should update an existing account atomically")
    void updateAccountShouldApplyUpdateFunction() {
        Account account = new Account("Test", new BigDecimal("100.0"));
        accountRepo.createAccount(account.getId(), account);

        Transaction deposit = new Transaction(
                "tx-1",
                LocalDateTime.of(2026, 4, 23, 12, 0),
                new BigDecimal("50.0"),
                TransactionType.DEPOSIT
        );

        Account updatedAccount = accountRepo.updateAccount(account.getId(),
                existing -> existing.addTransaction(deposit));

        assertNotNull(updatedAccount);
        assertEquals(new BigDecimal("150.0"), updatedAccount.getBalance());
        assertEquals(new BigDecimal("150.0"), accountRepo.getAccount(account.getId()).getBalance());
        assertEquals(1, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should wrap update failures in DatabaseUpdateException")
    void updateAccountShouldWrapUnexpectedFailures() {
        Account account = new Account("Test", new BigDecimal("100.0"));
        accountRepo.createAccount(account.getId(), account);
        String failureMessage = "Temporary database write error while updating account";

        DatabaseUpdateException exception = assertThrows(DatabaseUpdateException.class,
                () -> accountRepo.updateAccount(account.getId(), existing -> {
                    throw new IllegalStateException(failureMessage);
                }));

        assertEquals("Database update failed for account: " + account.getId(), exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals(failureMessage, exception.getCause().getMessage());
    }
}
