package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.FailedTransaction;
import com.teya.tinyledger.domain.Transaction;
import com.teya.tinyledger.dto.TransactionHistoryResponse;
import com.teya.tinyledger.dto.TransactionRequest;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.exception.DatabaseUpdateException;
import com.teya.tinyledger.exception.RetryableException;
import com.teya.tinyledger.queue.TransactionRetryQueue;
import com.teya.tinyledger.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static com.teya.tinyledger.domain.TransactionType.DEPOSIT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionRetryQueue retryQueue;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldAddTransactionSuccessfully() {
        String accountId = "acc-1";

        TransactionRequest request = new TransactionRequest(
                "tx-1",
                BigDecimal.TEN,
                DEPOSIT
        );

        Transaction result = transactionService.addTransaction(accountId, request);

        assertNotNull(result);
        assertEquals("tx-1", result.id());
        assertEquals(BigDecimal.TEN, result.amount());
        assertEquals(DEPOSIT, result.transactionType());

        verify(accountRepository).updateAccount(eq(accountId), any());
        verifyNoInteractions(retryQueue);
    }

    @Test
    void shouldRollbackTransacionSuccessfully() {
        String accountId = "acc-1";

        TransactionRequest request = new TransactionRequest(
                "tx-1",
                BigDecimal.TEN,
                DEPOSIT
        );

        Transaction result = transactionService.addTransaction(accountId, request);

        assertNotNull(result);
        assertEquals("tx-1", result.id());
        assertEquals(BigDecimal.TEN, result.amount());
        assertEquals(DEPOSIT, result.transactionType());

        verify(accountRepository).updateAccount(eq(accountId), any());
        verifyNoInteractions(retryQueue);

        transactionService.rollback(accountId);


    }

    @Test
    void shouldQueueAndThrowRetryableExceptionOnDatabaseError() {
        String accountId = "acc-1";

        TransactionRequest request = new TransactionRequest(
                "tx-1",
                BigDecimal.TEN,
                DEPOSIT
        );

        doThrow(new DatabaseUpdateException("DB failure"))
                .when(accountRepository)
                .updateAccount(eq(accountId), any());

        RetryableException ex = assertThrows(RetryableException.class, () ->
                transactionService.addTransaction(accountId, request)
        );

        assertTrue(ex.getMessage().contains("RetryableException"));

        ArgumentCaptor<FailedTransaction> captor =
                ArgumentCaptor.forClass(FailedTransaction.class);

        verify(retryQueue).addToRetryQueue(captor.capture());

        FailedTransaction failedTransaction = captor.getValue();
        assertEquals(accountId, failedTransaction.getAccountId());
        assertEquals("tx-1", failedTransaction.getTransaction().id());
    }

    @Test
    void shouldPropagateAccountNotFoundExceptionWithoutQueueing() {
        String accountId = "acc-1";

        TransactionRequest request = new TransactionRequest(
                "tx-1",
                BigDecimal.TEN,
                DEPOSIT
        );

        doThrow(new AccountNotFoundException("not found"))
                .when(accountRepository)
                .updateAccount(eq(accountId), any());

        assertThrows(AccountNotFoundException.class, () ->
                transactionService.addTransaction(accountId, request)
        );

        verifyNoInteractions(retryQueue);
    }

    @Test
    void retryAddTransactionShouldCallRepositoryUpdate() {
        String accountId = "acc-1";

        Transaction transaction = new Transaction(
                "tx-1",
                LocalDateTime.now(),
                BigDecimal.TEN,
                DEPOSIT
        );

        transactionService.retryAddTransaction(accountId, transaction);

        verify(accountRepository).updateAccount(eq(accountId), any());
    }

    @Test
    void retryAddTransactionShouldPropagateRetryableException() {
        String accountId = "acc-1";

        Transaction transaction = new Transaction(
                "tx-1",
                LocalDateTime.now(),
                BigDecimal.TEN,
                DEPOSIT
        );

        doThrow(new DatabaseUpdateException("fail"))
                .when(accountRepository)
                .updateAccount(eq(accountId), any());

        assertThrows(RetryableException.class, () ->
                transactionService.retryAddTransaction(accountId, transaction)
        );
    }

    @Test
    void shouldReturnSortedPaginatedTransactionHistory() {
        String accountId = "acc-1";

        Transaction older = new Transaction(
                "tx-1",
                LocalDateTime.now().minusDays(2),
                BigDecimal.ONE,
                DEPOSIT
        );

        Transaction newest = new Transaction(
                "tx-2",
                LocalDateTime.now(),
                BigDecimal.TEN,
                DEPOSIT
        );

        Transaction middle = new Transaction(
                "tx-3",
                LocalDateTime.now().minusDays(1),
                BigDecimal.ZERO,
                DEPOSIT
        );

        Account account = mock(Account.class);

        when(account.getId()).thenReturn(accountId);
        when(account.getName()).thenReturn("Test");
        when(account.getBalance()).thenReturn(BigDecimal.TEN);
        when(account.getTransactions()).thenReturn(Set.of(older, newest, middle));

        when(accountService.validateAccountExists(accountId)).thenReturn(account);

        TransactionHistoryResponse response = transactionService.getTransactionHistory(accountId, 0, 2);

        assertEquals(2, response.getTransactions().size());

        // DESC order
        assertEquals("tx-2", response.getTransactions().get(0).id());
        assertEquals("tx-3", response.getTransactions().get(1).id());
    }

    @Test
    void shouldClampPageSizeAndNormalizePage() {
        String accountId = "acc-1";

        Transaction tx = new Transaction(
                "tx-1",
                LocalDateTime.now(),
                BigDecimal.ONE,
                DEPOSIT
        );

        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getName()).thenReturn("Test");
        when(account.getBalance()).thenReturn(BigDecimal.ONE);
        when(account.getTransactions()).thenReturn(Set.of(tx));

        when(accountService.validateAccountExists(accountId)).thenReturn(account);

        TransactionHistoryResponse response =
                transactionService.getTransactionHistory(accountId, -5, 1000);

        assertEquals(1, response.getTransactions().size());
    }
}