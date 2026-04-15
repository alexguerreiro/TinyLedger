package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.OperationType;
import com.teya.tinyledger.domain.Transaction;
import com.teya.tinyledger.dto.TransactionHistoryResponse;
import com.teya.tinyledger.dto.TransactionRequest;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.repository.AccountRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        Account account = new Account("Test", 1000.0);
        UUID accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        Double depositAmount = 500.0;
        TransactionRequest request = new TransactionRequest(accountId, depositAmount);

        transactionService.addDeposit(request);

        Account updatedAccount = accountRepo.getAccount(accountId);
        assertNotNull(updatedAccount);
        assertEquals(1500.0, updatedAccount.getBalance());
        assertEquals(1, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should successfully add withdrawal from account")
    void testAddWithdrawal_Success() {
        Account account = new Account("Test", 1000.0);
        UUID accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        Double withdrawalAmount = 300.0;
        TransactionRequest request = new TransactionRequest(accountId, withdrawalAmount);

        transactionService.addWithdrawal(request);

        Account updatedAccount = accountRepo.getAccount(accountId);
        assertNotNull(updatedAccount);
        assertEquals(700.0, updatedAccount.getBalance());
        assertEquals(1, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should throw exception when account not found for deposit")
    void testAddDeposit_AccountNotFound() {
        UUID nonExistentAccountId = UUID.randomUUID();
        Double depositAmount = 100.0;
        TransactionRequest request = new TransactionRequest(nonExistentAccountId, depositAmount);

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.addDeposit(request);
        });

        assertTrue(exception.getMessage().contains("Account not found"));
    }

    @Test
    @DisplayName("Should throw exception when account not found for withdrawal")
    void testAddWithdrawal_AccountNotFound() {
        UUID nonExistentAccountId = UUID.randomUUID();
        Double withdrawalAmount = 100.0;
        TransactionRequest request = new TransactionRequest(nonExistentAccountId, withdrawalAmount);

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.addWithdrawal(request);
        });

        assertTrue(exception.getMessage().contains("Account not found"));
    }

    @Test
    @DisplayName("Should retrieve transaction history sorted by created at (most recent first)")
    void testGetTransactionHistory_Success() {
        Account account = new Account("Test User", 250.0);
        UUID accountId = account.getId();

        // Create transactions with different timestamps
        Transaction t1 = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 4, 15, 10, 0, 0),
                100.0,
                OperationType.DEPOSIT
        );
        Transaction t2 = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 4, 15, 11, 0, 0),
                50.0,
                OperationType.WITHDRAWAL
        );
        Transaction t3 = new Transaction(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 4, 15, 9, 0, 0),
                200.0,
                OperationType.DEPOSIT
        );

        account.getTransactions().add(t1);
        account.getTransactions().add(t2);
        account.getTransactions().add(t3);
        accountRepo.saveAccount(accountId, account);

        TransactionHistoryResponse history = transactionService.getTransactionHistory(accountId);

        assertNotNull(history);
        assertEquals(accountId, history.getAccountId());
        assertEquals("Test User", history.getAccountName());
        assertEquals(250.0, history.getCurrentBalance());
        assertEquals(3, history.getTransactionCount());
        assertEquals(3, history.getTransactions().size());
    }

    @Test
    @DisplayName("Should throw exception when account not found for transaction history")
    void testGetTransactionHistory_AccountNotFound() {
        UUID nonExistentAccountId = UUID.randomUUID();

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            transactionService.getTransactionHistory(nonExistentAccountId);
        });

        assertTrue(exception.getMessage().contains("Account not found"));
    }

    @Test
    @DisplayName("Should return empty transaction history for account with no transactions")
    void testGetTransactionHistory_EmptyTransactions() {
        Account account = new Account("No Transactions", 1000.0);
        UUID accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        TransactionHistoryResponse history = transactionService.getTransactionHistory(accountId);

        assertNotNull(history);
        assertEquals(0, history.getTransactionCount());
        assertEquals(0, history.getTransactions().size());
        assertEquals(1000.0, history.getCurrentBalance());
    }


    @Test
    @DisplayName("Should handle mixed deposit and withdrawal operations")
    void testMixedOperations() {
        Account account = new Account("Mixed Operations", 1000.0);
        UUID accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        TransactionRequest depositRequest1 = new TransactionRequest(accountId, 500.0);
        TransactionRequest withdrawalRequest1 = new TransactionRequest(accountId, 200.0);
        TransactionRequest depositRequest2 = new TransactionRequest(accountId, 300.0);
        TransactionRequest withdrawalRequest2 = new TransactionRequest(accountId, 100.0);

        transactionService.addDeposit(depositRequest1);      // 1500
        transactionService.addWithdrawal(withdrawalRequest1); // 1300
        transactionService.addDeposit(depositRequest2);      // 1600
        transactionService.addWithdrawal(withdrawalRequest2); // 1500

        Account updatedAccount = accountRepo.getAccount(accountId);
        assertNotNull(updatedAccount);
        assertEquals(1500.0, updatedAccount.getBalance());
        assertEquals(4, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should verify deposit increases balance correctly")
    void testDeposit_BalanceIncrement() {
        Account account = new Account("Balance Test", 500.0);
        UUID accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        TransactionRequest request = new TransactionRequest(accountId, 250.0);

        transactionService.addDeposit(request);

        Account updatedAccount = accountRepo.getAccount(accountId);
        assertEquals(750.0, updatedAccount.getBalance());
    }

    @Test
    @DisplayName("Should verify withdrawal decreases balance correctly")
    void testWithdrawal_BalanceDecrement() {
        Account account = new Account("Withdrawal Test", 1000.0);
        UUID accountId = account.getId();
        accountRepo.saveAccount(accountId, account);

        TransactionRequest request = new TransactionRequest(accountId, 350.0);

        transactionService.addWithdrawal(request);

        Account updatedAccount = accountRepo.getAccount(accountId);
        assertEquals(650.0, updatedAccount.getBalance());
    }
}


