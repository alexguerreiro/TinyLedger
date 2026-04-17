package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.FailedTransaction;
import com.teya.tinyledger.domain.TransactionType;
import com.teya.tinyledger.domain.Transaction;
import com.teya.tinyledger.dto.TransactionRequest;
import com.teya.tinyledger.dto.TransactionHistoryResponse;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.exception.DatabaseUpdateException;
import com.teya.tinyledger.queue.TransactionQueue;
import com.teya.tinyledger.repository.AccountRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static com.teya.tinyledger.domain.TransactionType.*;

@Service
public class TransactionService {
    private final AccountRepo accountRepo;
    private final TransactionQueue transactionQueue;
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(AccountRepo accountRepo, TransactionQueue transactionQueue) {
        this.accountRepo = accountRepo;
        this.transactionQueue = transactionQueue;
    }


    /**
     * Processes a transaction request. If the transaction fails with a DatabaseUpdateException,
     * the transaction is automatically added to the retry queue (TransactionRetryScheduler will retry it).
     *
     * @param transactionRequest the transaction request to process
     * @throws DatabaseUpdateException if a database error occurs during transaction processing (retryable)
     * @throws AccountNotFoundException if the account is not found
     */
    public void createTransaction(TransactionRequest transactionRequest) {
        try {
            createTransactionInternal(transactionRequest);
        } catch (DatabaseUpdateException e) {
            logger.error("Transaction creation failed with database error for account: {}. Adding to retry queue.",
                transactionRequest.getAccountId(), e);

            FailedTransaction failedTransaction = new FailedTransaction(transactionRequest);
            transactionQueue.addToRetryQueue(failedTransaction);

            throw e;
        }
    }

    /**
     * Internal method to process the transaction logic.
     * This is called both by createTransaction and by the retry scheduler.
     *
     * @param transactionRequest the transaction request to process
     * @throws AccountNotFoundException if the account is not found
     */
    public void createTransactionInternal(TransactionRequest transactionRequest) {
        Transaction transaction = buildTransaction(transactionRequest.getAmount(), transactionRequest.getTransactionType());

        Account updatedAccount = accountRepo.updateAccountAtomically(transactionRequest.getAccountId(),
                account -> {
                    Account withTransaction = account.withTransaction(transaction);

                    // Calculate new balance
                    BigDecimal newBalance = transactionRequest.getTransactionType() == DEPOSIT
                            ? withTransaction.getBalance().add(transaction.amount())
                            : withTransaction.getBalance().subtract(transaction.amount());

                    // Return account with updated balance
                    return withTransaction.withBalance(newBalance);
                });

        if (updatedAccount == null) {
            logger.error("Account not found for id: {}", transactionRequest.getAccountId());
            throw new AccountNotFoundException("Account not found for id: " + transactionRequest.getAccountId());
        }
    }

    private Transaction buildTransaction(BigDecimal amount, TransactionType transactionType) {
        return new Transaction(UUID.randomUUID().toString(), LocalDateTime.now(), amount, transactionType);
    }

    public TransactionHistoryResponse getTransactionHistory(String accountId) {
        Account account = accountRepo.getAccount(accountId);
        if (account == null) {
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

}
