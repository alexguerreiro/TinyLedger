package com.teya.tinyledger.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Account Unit Tests")
class AccountTest {

    @Test
    @DisplayName("Should apply deposit transaction to balance and history")
    void applyTransaction_ShouldHandleDeposit() {
        Account account = new Account("Test", new BigDecimal("100.0"));
        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                LocalDateTime.of(2026, 4, 23, 10, 0),
                new BigDecimal("25.0"),
                TransactionType.DEPOSIT
        );

        Account updatedAccount = account.applyTransaction(transaction);

        assertEquals(new BigDecimal("125.0"), updatedAccount.getBalance());
        assertEquals(1, updatedAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should apply withdrawal transaction to balance and history")
    void applyTransaction_ShouldHandleWithdrawal() {
        Account account = new Account("Test", new BigDecimal("100.0"));
        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                LocalDateTime.of(2026, 4, 23, 10, 0),
                new BigDecimal("25.0"),
                TransactionType.WITHDRAWAL
        );

        Account updatedAccount = account.applyTransaction(transaction);

        assertEquals(new BigDecimal("75.0"), updatedAccount.getBalance());
        assertEquals(1, updatedAccount.getTransactions().size());
    }
}
