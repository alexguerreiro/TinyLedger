package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.TransactionType;
import com.teya.tinyledger.domain.Transaction;
import com.teya.tinyledger.dto.TransactionHistoryResponse;
import com.teya.tinyledger.dto.TransactionRequest;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.repository.AccountRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionService Integration Tests")
class TransactionServiceTest {

    private AccountRepo accountRepo;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        accountRepo = new AccountRepo();
        transactionService = new TransactionService(accountRepo);
    }

    @Test
    @DisplayName("Should successfully add deposit to account")
    void testAddDeposit_Success() {
        Account account = new Account("Test", new BigDecimal("1000.0"));
        String accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        BigDecimal depositAmount = new BigDecimal("500.0");
        TransactionRequest request = new TransactionRequest(accountId, depositAmount, TransactionType.DEPOSIT);

        transactionService.createTransaction(request);

        Account updatedAccount = accountRepo.getAccount(accountId);
        assertNotNull(updatedAccount);
        assertEquals(new BigDecimal("1500.0"), updatedAccount.getBalance());
        assertEquals(1, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should successfully add withdrawal from account")
    void testAddWithdrawal_Success() {
        Account account = new Account("Test", new BigDecimal("1000.0"));
        String accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        BigDecimal withdrawalAmount = new BigDecimal("300.0");
        TransactionRequest request = new TransactionRequest(accountId, withdrawalAmount, TransactionType.WITHDRAWAL);

        transactionService.createTransaction(request);

        Account updatedAccount = accountRepo.getAccount(accountId);
        assertNotNull(updatedAccount);
        assertEquals(new BigDecimal("700.0"), updatedAccount.getBalance());
        assertEquals(1, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should throw exception when account not found for deposit")
    void testAddDeposit_AccountNotFound() {
        String nonExistentAccountId = UUID.randomUUID().toString();
        BigDecimal depositAmount = new BigDecimal("100.0");
        TransactionRequest request = new TransactionRequest(nonExistentAccountId, depositAmount, TransactionType.DEPOSIT);

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertTrue(exception.getMessage().contains("Account not found"));
    }

    @Test
    @DisplayName("Should throw exception when account not found for withdrawal")
    void testAddWithdrawal_AccountNotFound() {
        String nonExistentAccountId = UUID.randomUUID().toString();
        BigDecimal withdrawalAmount = new BigDecimal("100.0");
        TransactionRequest request = new TransactionRequest(nonExistentAccountId, withdrawalAmount, TransactionType.WITHDRAWAL);

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.createTransaction(request);
        });

        assertTrue(exception.getMessage().contains("Account not found"));
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

        account.addTransaction(t1);
        account.addTransaction(t2);
        account.addTransaction(t3);
        accountRepo.saveAccount(accountId, account);

        TransactionHistoryResponse history = transactionService.getTransactionHistory(accountId);

        assertNotNull(history);
        assertEquals(accountId, history.getAccountId());
        assertEquals("Test User", history.getAccountName());
        assertEquals(new BigDecimal("250.0"), history.getCurrentBalance());
        assertEquals(3, history.getTransactionCount());
        assertEquals(3, history.getTransactions().size());
    }

    @Test
    @DisplayName("Should throw exception when account not found for transaction history")
    void testGetTransactionHistory_AccountNotFound() {
        String nonExistentAccountId = UUID.randomUUID().toString();

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.getTransactionHistory(nonExistentAccountId);
        });

        assertTrue(exception.getMessage().contains("Account not found"));
    }

    @Test
    @DisplayName("Should return empty transaction history for account with no transactions")
    void testGetTransactionHistory_EmptyTransactions() {
        Account account = new Account("No Transactions", new BigDecimal("1000.0"));
        String accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        TransactionHistoryResponse history = transactionService.getTransactionHistory(accountId);

        assertNotNull(history);
        assertEquals(0, history.getTransactionCount());
        assertEquals(0, history.getTransactions().size());
        assertEquals(new BigDecimal("1000.0"), history.getCurrentBalance());
    }


    @Test
    @DisplayName("Should handle mixed deposit and withdrawal operations")
    void testMixedOperations() {
        Account account = new Account("Mixed Operations", new BigDecimal("1000.0"));
        String accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        TransactionRequest depositRequest1 = new TransactionRequest(accountId, new BigDecimal("500.0"), TransactionType.DEPOSIT);
        TransactionRequest withdrawalRequest1 = new TransactionRequest(accountId, new BigDecimal("200.0"), TransactionType.WITHDRAWAL);
        TransactionRequest depositRequest2 = new TransactionRequest(accountId, new BigDecimal("300.0"), TransactionType.DEPOSIT);
        TransactionRequest withdrawalRequest2 = new TransactionRequest(accountId, new BigDecimal("100.0"), TransactionType.WITHDRAWAL);

        transactionService.createTransaction(depositRequest1);      // 1500
        transactionService.createTransaction(withdrawalRequest1); // 1300
        transactionService.createTransaction(depositRequest2);      // 1600
        transactionService.createTransaction(withdrawalRequest2); // 1500

        Account updatedAccount = accountRepo.getAccount(accountId);
        assertNotNull(updatedAccount);
        assertEquals(new BigDecimal("1500.0"), updatedAccount.getBalance());
        assertEquals(4, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should verify deposit increases balance correctly")
    void testDeposit_BalanceIncrement() {
        Account account = new Account("Balance Test", new BigDecimal("500.0"));
        String accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        TransactionRequest request = new TransactionRequest(accountId, new BigDecimal("250.0"), TransactionType.DEPOSIT);

        transactionService.createTransaction(request);

        Account updatedAccount = accountRepo.getAccount(accountId);
        assertEquals(new BigDecimal("750.0"), updatedAccount.getBalance());
    }

    @Test
    @DisplayName("Should verify withdrawal decreases balance correctly")
    void testWithdrawal_BalanceDecrement() {
        Account account = new Account("Withdrawal Test", new BigDecimal("1000.0"));
        String accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        TransactionRequest request = new TransactionRequest(accountId, new BigDecimal("350.0"), TransactionType.WITHDRAWAL);

        transactionService.createTransaction(request);

        Account updatedAccount = accountRepo.getAccount(accountId);
        assertEquals(new BigDecimal("650.0"), updatedAccount.getBalance());
    }
}


