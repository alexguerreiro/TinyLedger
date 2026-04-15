package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.OperationType;
import com.teya.tinyledger.domain.Transaction;
import com.teya.tinyledger.dto.TransactionRequest;
import com.teya.tinyledger.dto.TransactionHistoryResponse;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.repository.AccountRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static com.teya.tinyledger.domain.OperationType.*;

@Service
public class TransactionService {
    private final AccountRepo accountRepo;
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    public void addDeposit(TransactionRequest transactionRequest) {
        Transaction transaction = buildTransaction(transactionRequest.getAmount(), DEPOSIT);
        deposit(transaction, transactionRequest.getAccountId());
    }

    public void addWithdrawal(TransactionRequest transactionRequest) {
        Transaction transaction = buildTransaction(transactionRequest.getAmount(), WITHDRAWAL);
        withdrawal(transaction, transactionRequest.getAccountId());
    }

    public TransactionHistoryResponse getTransactionHistory(UUID accountId) {
        Account account = accountRepo.getAccount(accountId);
        if(account == null) {
            logger.error("Account not found for id: {}", accountId);
            throw new AccountNotFoundException("Account not found for id: " + accountId);
        }

        // Sort transactions by created at in descending order (most recent first)
        Set<Transaction> sortedTransactions = account.getTransactions().stream()
                .sorted((t1, t2) -> t2.createdAt().compareTo(t1.createdAt()))
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        return new TransactionHistoryResponse(
                account.getId(),
                account.getName(),
                account.getBalance(),
                sortedTransactions
        );
    }

    private void deposit(Transaction transaction, UUID accountId) {
        Account updatedAccount = accountRepo.updateAccountAtomically(accountId, account -> {
            account.addTransaction(transaction);
            account.setBalance(account.getBalance() + transaction.amount());
            return account;
        });

        if (updatedAccount == null) {
            logger.error("Account not found for id: {}", accountId);
            throw new AccountNotFoundException("Account not found for id: " + accountId);
        }
    }

    private void withdrawal(Transaction transaction, UUID userId) {
        Account updatedAccount = accountRepo.updateAccountAtomically(userId, account -> {
            account.addTransaction(transaction);
            account.setBalance(account.getBalance() - transaction.amount());
            return account;
        });

        if (updatedAccount == null) {
            logger.error("Account not found for user id: {}", userId);
            throw new AccountNotFoundException("Account not found for user id: " + userId);
        }
    }

    private Transaction buildTransaction(double amount, OperationType operationType) {
        return new Transaction(UUID.randomUUID(), LocalDateTime.now(), amount, operationType);
    }
}
