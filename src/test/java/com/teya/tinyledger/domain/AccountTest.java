package com.teya.tinyledger.domain;

import com.teya.tinyledger.exception.InvalidTransactionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldCreateAccountSuccessfully() {
        Account account = new Account("Test Account", BigDecimal.valueOf(100));

        assertNotNull(account.getId());
        assertEquals("Test Account", account.getName());
        assertEquals(BigDecimal.valueOf(100), account.getBalance());
        assertTrue(account.getTransactions().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenCreatingAccountWithNegativeBalance() {
        assertThrows(IllegalArgumentException.class,
                () -> new Account("Invalid Account", BigDecimal.valueOf(-1)));
    }

    @Test
    void shouldApplyDepositTransaction() {
        Account account = new Account("Test", BigDecimal.valueOf(100));

        Transaction deposit = new Transaction(
                "tx1",
                LocalDateTime.now(),
                BigDecimal.valueOf(50),
                TransactionType.DEPOSIT
        );

        Account updated = account.addTransaction(deposit);

        assertEquals(BigDecimal.valueOf(150), updated.getBalance());
        assertTrue(updated.getTransactions().contains(deposit));

        // original remains unchanged (immutability)
        assertEquals(BigDecimal.valueOf(100), account.getBalance());
        assertTrue(account.getTransactions().isEmpty());
    }

    @Test
    void shouldApplyWithdrawalTransaction() {
        Account account = new Account("Test", BigDecimal.valueOf(100));

        Transaction withdrawal = new Transaction(
                "tx2",
                LocalDateTime.now(),
                BigDecimal.valueOf(40),
                TransactionType.WITHDRAWAL
        );

        Account updated = account.addTransaction(withdrawal);

        assertEquals(BigDecimal.valueOf(60), updated.getBalance());
        assertTrue(updated.getTransactions().contains(withdrawal));
    }

    @Test
    void shouldThrowExceptionForDuplicateTransaction() {
        Account account = new Account("Test", BigDecimal.valueOf(100));

        Transaction tx = new Transaction(
                "tx-dup",
                LocalDateTime.now(),
                BigDecimal.valueOf(10),
                TransactionType.DEPOSIT
        );

        Account updated = account.addTransaction(tx);

        assertThrows(InvalidTransactionException.class,
                () -> updated.addTransaction(tx));
    }

    @Test
    void transactionsShouldBeImmutable() {
        Account account = new Account("Test", BigDecimal.valueOf(100));

        Transaction tx = new Transaction(
                "tx1",
                LocalDateTime.now(),
                BigDecimal.valueOf(10),
                TransactionType.DEPOSIT
        );

        Account updated = account.addTransaction(tx);

        Set<Transaction> transactions = updated.getTransactions();

        assertThrows(UnsupportedOperationException.class,
                () -> transactions.add(tx));
    }
}