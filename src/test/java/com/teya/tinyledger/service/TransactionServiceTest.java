package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.Transaction;
import com.teya.tinyledger.domain.TransactionType;
import com.teya.tinyledger.dto.TransactionHistoryResponse;
import com.teya.tinyledger.dto.TransactionRequest;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.exception.DatabaseUpdateException;
import com.teya.tinyledger.queue.TransactionQueue;
import com.teya.tinyledger.repository.AccountRepository;
import com.teya.tinyledger.repository.InMemoryAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("TransactionService Integration Tests")
class TransactionServiceTest {
    private static final boolean RETRY = true;
    private AccountRepository accountRepository;
    private AccountService accountService;
    private TransactionQueue transactionQueue;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        accountRepository = new InMemoryAccountRepository();
        accountService = new AccountService(accountRepository);
        transactionQueue = new TransactionQueue();
        transactionService = new TransactionService(accountRepository, accountService, transactionQueue);
    }

    @Test
    @DisplayName("Should successfully add deposit to account")
    void testAddDeposit_Success() {
        Account account = new Account("Test", new BigDecimal("1000.0"));
        String accountId = account.getId();
        accountRepository.createAccount(accountId, account);

        BigDecimal depositAmount = new BigDecimal("500.0");
        TransactionRequest request = new TransactionRequest(depositAmount, TransactionType.DEPOSIT);

        transactionService.addTransaction(accountId, request, RETRY);

        Account updatedAccount = accountRepository.getAccount(accountId);
        assertNotNull(updatedAccount);
        assertEquals(new BigDecimal("1500.0"), updatedAccount.getBalance());
        assertEquals(1, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should successfully add withdrawal from account")
    void testAddWithdrawal_Success() {
        Account account = new Account("Test", new BigDecimal("1000.0"));
        String accountId = account.getId();
        accountRepository.createAccount(accountId, account);

        BigDecimal withdrawalAmount = new BigDecimal("300.0");
        TransactionRequest request = new TransactionRequest(withdrawalAmount, TransactionType.WITHDRAWAL);

        transactionService.addTransaction(accountId, request, RETRY);

        Account updatedAccount = accountRepository.getAccount(accountId);
        assertNotNull(updatedAccount);
        assertEquals(new BigDecimal("700.0"), updatedAccount.getBalance());
        assertEquals(1, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should throw exception when account not found for deposit")
    void testAddDeposit_AccountNotFound() {
        String nonExistentAccountId = UUID.randomUUID().toString();
        BigDecimal depositAmount = new BigDecimal("100.0");
        TransactionRequest request = new TransactionRequest(depositAmount, TransactionType.DEPOSIT);

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.addTransaction(nonExistentAccountId, request, RETRY);
        });

        assertTrue(exception.getMessage().contains("Account not found"));
        // Verify no transaction was added to the retry queue
        assertEquals(0, transactionQueue.getRetryQueueSize());
    }

    @Test
    @DisplayName("Should throw exception when account not found for withdrawal")
    void testAddWithdrawal_AccountNotFound() {
        String nonExistentAccountId = UUID.randomUUID().toString();
        BigDecimal withdrawalAmount = new BigDecimal("100.0");
        TransactionRequest request = new TransactionRequest(withdrawalAmount, TransactionType.WITHDRAWAL);

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.addTransaction(nonExistentAccountId, request, RETRY);
        });

        assertTrue(exception.getMessage().contains("Account not found"));
        // Verify no transaction was added to the retry queue
        assertEquals(0, transactionQueue.getRetryQueueSize());
    }

    @Test
    @DisplayName("Should throw exception when database is not available and transaction should be sent to retry queue")
    void testAddDeposit_DatabaseError() {
        String accountId = UUID.randomUUID().toString();
        AccountRepository accountRepository = new AccountRepository() {
            @Override
            public void createAccount(String ignoredAccountId, Account account) {
            }

            @Override
            public Account getAccount(String ignoredAccountId) {
                return new Account("Test", new BigDecimal("1000.0"));
            }

            @Override
            public Account updateAccount(String ignoredAccountId, UnaryOperator<Account> updateFunction) {
                throw new DatabaseUpdateException("DB down");
            }
        };
        AccountService accountService = new AccountService(accountRepository);
        TransactionQueue transactionQueue = new TransactionQueue();
        TransactionService transactionService = new TransactionService(accountRepository, accountService, transactionQueue);

        TransactionRequest request =
                new TransactionRequest(new BigDecimal("100.0"), TransactionType.DEPOSIT);

        DatabaseUpdateException exception = assertThrows(DatabaseUpdateException.class, () -> {
            transactionService.addTransaction(accountId, request, RETRY);
        });

        assertEquals("DB down", exception.getMessage());

        // Verify retry queue was used
        assertEquals(1, transactionQueue.getRetryQueueSize());
    }

    @Test
    @DisplayName("Should handle mixed deposit and withdrawal operations")
    void testMixedOperations() {
        Account account = new Account("Mixed Operations", new BigDecimal("1000.0"));
        String accountId = account.getId();
        accountRepository.createAccount(accountId, account);

        TransactionRequest depositRequest1 = new TransactionRequest(new BigDecimal("500.0"), TransactionType.DEPOSIT);
        TransactionRequest withdrawalRequest1 = new TransactionRequest(new BigDecimal("200.0"), TransactionType.WITHDRAWAL);
        TransactionRequest depositRequest2 = new TransactionRequest(new BigDecimal("300.0"), TransactionType.DEPOSIT);
        TransactionRequest withdrawalRequest2 = new TransactionRequest(new BigDecimal("100.0"), TransactionType.WITHDRAWAL);

        transactionService.addTransaction(accountId, depositRequest1, RETRY);      // 1500
        transactionService.addTransaction(accountId, withdrawalRequest1, RETRY); // 1300
        transactionService.addTransaction(accountId, depositRequest2, RETRY);      // 1600
        transactionService.addTransaction(accountId, withdrawalRequest2, RETRY); // 1500

        Account updatedAccount = accountRepository.getAccount(accountId);
        assertNotNull(updatedAccount);
        assertEquals(new BigDecimal("1500.0"), updatedAccount.getBalance());
        assertEquals(4, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should verify deposit increases balance correctly")
    void testDeposit_BalanceIncrement() {
        Account account = new Account("Balance Test", new BigDecimal("500.0"));
        String accountId = account.getId();
        accountRepository.createAccount(accountId, account);

        TransactionRequest request = new TransactionRequest(new BigDecimal("250.0"), TransactionType.DEPOSIT);

        transactionService.addTransaction(accountId, request, RETRY);

        Account updatedAccount = accountRepository.getAccount(accountId);
        assertEquals(new BigDecimal("750.0"), updatedAccount.getBalance());
    }

    @Test
    @DisplayName("Should verify withdrawal decreases balance correctly")
    void testWithdrawal_BalanceDecrement() {
        Account account = new Account("Withdrawal Test", new BigDecimal("1000.0"));
        String accountId = account.getId();
        accountRepository.createAccount(accountId, account);

        TransactionRequest request = new TransactionRequest(new BigDecimal("350.0"), TransactionType.WITHDRAWAL);

        transactionService.addTransaction(accountId, request, RETRY);

        Account updatedAccount = accountRepository.getAccount(accountId);
        assertEquals(new BigDecimal("650.0"), updatedAccount.getBalance());
    }

    @Test
    @DisplayName("Should retrieve transaction history sorted by created at (most recent first)")
    void testGetTransactionHistory_Success() {
        Account account = new Account("Test User", new BigDecimal("250.0"));
        String accountId = account.getId();

        // Create transactions with different date times
        Transaction t1 = new Transaction(
                UUID.randomUUID().toString(),
                LocalDateTime.of(2026, 4, 15, 10, 0, 0),
                new BigDecimal("100.0"),
                TransactionType.DEPOSIT
        );
        Transaction t2 = new Transaction(
                UUID.randomUUID().toString(),
                LocalDateTime.of(2026, 4, 15, 11, 0, 0),
                new BigDecimal("50.0"),
                TransactionType.WITHDRAWAL
        );
        Transaction t3 = new Transaction(
                UUID.randomUUID().toString(),
                LocalDateTime.of(2026, 4, 15, 9, 0, 0),
                new BigDecimal("200.0"),
                TransactionType.DEPOSIT
        );

        Account accountWithT1 = account.applyTransaction(t1);
        Account accountWithT2 = accountWithT1.applyTransaction(t2);
        Account accountWithT3 = accountWithT2.applyTransaction(t3);
        accountRepository.createAccount(accountId, accountWithT3);

        TransactionHistoryResponse history = transactionService.getTransactionHistory(accountId, 0, 10);

        assertNotNull(history);
        assertEquals(accountId, history.getAccountId());
        assertEquals("Test User", history.getAccountName());
        assertEquals(new BigDecimal("500.0"), history.getCurrentBalance());
        assertEquals(3, history.getTransactionCount());
        assertEquals(3, history.getTransactions().size());

        List<Transaction> transactions = history.getTransactions().stream().toList();
        assertEquals(t2.id(), transactions.get(0).id());
        assertEquals(t1.id(), transactions.get(1).id());
        assertEquals(t3.id(), transactions.get(2).id());
    }

    @Test
    @DisplayName("Should throw exception when account not found for transaction history")
    void testGetTransactionHistory_AccountNotFound() {
        String nonExistentAccountId = UUID.randomUUID().toString();

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.getTransactionHistory(nonExistentAccountId, 0, 20);
        });

        assertTrue(exception.getMessage().contains("Account not found"));
    }

    @Test
    @DisplayName("Should return empty transaction history for account with no transactions")
    void testGetTransactionHistory_EmptyTransactions() {
        Account account = new Account("No Transactions", new BigDecimal("1000.0"));
        String accountId = account.getId();
        accountRepository.createAccount(accountId, account);

        TransactionHistoryResponse history = transactionService.getTransactionHistory(accountId, 0, 20);

        assertNotNull(history);
        assertEquals(0, history.getTransactionCount());
        assertEquals(0, history.getTransactions().size());
        assertEquals(new BigDecimal("1000.0"), history.getCurrentBalance());
    }
}
