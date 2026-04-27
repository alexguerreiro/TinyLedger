package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.Account;
import com.teya.tinyledger.domain.FailedTransaction;
import com.teya.tinyledger.domain.Transaction;
import com.teya.tinyledger.dto.TransactionHistoryResponse;
import com.teya.tinyledger.dto.TransactionRequest;
import com.teya.tinyledger.exception.AccountNotFoundException;
import com.teya.tinyledger.exception.DatabaseUpdateException;
import com.teya.tinyledger.exception.InvalidTransactionException;
import com.teya.tinyledger.exception.RetryableException;
import com.teya.tinyledger.queue.TransactionRetryQueue;
import com.teya.tinyledger.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final TransactionRetryQueue transactionRetryQueue;

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(AccountRepository accountRepository,
                              AccountService accountService,
                              TransactionRetryQueue transactionRetryQueue) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.transactionRetryQueue = transactionRetryQueue;
    }


    /**
     * Processes a transaction request. If the transaction fails with a DatabaseUpdateException,
     * the transaction is automatically added to the retry queue (TransactionRetryScheduler will retry it).
     *
     * @param accountId id of the account to be updated
     * @param transactionRequest the transaction request to process
     * @throws RetryableException if a (hypothetical) database error occurs during transaction processing (retryable)
     * @throws AccountNotFoundException if the account is not found
     * @throws InvalidTransactionException if the transaction is invalid (ex duplicated transaction)
     */
    public Transaction addTransaction(String accountId, TransactionRequest transactionRequest) {
        Transaction transaction = buildTransaction(transactionRequest);

        try {
            accountRepository.updateAccount(accountId, account -> account.addTransaction(transaction));
            return transaction;
        } catch (DatabaseUpdateException e) {
            logger.error("Transaction creation failed with database error for account: {}.", accountId, e);
            addTransactionToRetryQueue(accountId, transaction);
            throw new RetryableException("RetryableException occurred - transaction queued for retry", e);
        }
    }

    /**
     * Retry to update a transaction.
     *
     * @param accountId id of the account to be updated
     * @param transaction the transaction to process
     * @throws DatabaseUpdateException if a database error occurs during transaction processing (retryable)
     * @throws AccountNotFoundException if the account is not found
     * @throws InvalidTransactionException if the transaction is invalid (ex duplicated transaction)
     */
    public void retry(String accountId, Transaction transaction) {
        accountRepository.updateAccount(accountId, account -> account.addTransaction(transaction));
    }

    public TransactionHistoryResponse getTransactionHistory(String accountId, int page, int pageSize) {
        Account account = accountService.validateAccountExists(accountId);

        int safePage = Math.max(page, 0);
        int safePageSize = Math.clamp(pageSize, 1, 100); // cap at 100

        List<Transaction> pagedTransactions = account.getTransactions().stream()
                .sorted((t1, t2) -> t2.createdAt().compareTo(t1.createdAt()))
                .skip((long) safePage * safePageSize)
                .limit(safePageSize)
                .toList();

        return new TransactionHistoryResponse(
                account.getId(),
                account.getName(),
                account.getBalance(),
                pagedTransactions
        );
    }

    private Transaction buildTransaction(TransactionRequest transactionRequest) {
        return new Transaction(
                transactionRequest.getTransactionId(),
                LocalDateTime.now(),
                transactionRequest.getAmount(),
                transactionRequest.getTransactionType()
        );
    }

    private void addTransactionToRetryQueue(String accountId, Transaction transaction){
        FailedTransaction failedTransaction = new FailedTransaction(accountId, transaction);
        transactionRetryQueue.addToRetryQueue(failedTransaction);
    }
}
