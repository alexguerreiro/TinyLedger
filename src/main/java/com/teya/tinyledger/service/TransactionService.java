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
import com.teya.tinyledger.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.teya.tinyledger.domain.TransactionType.*;

@Service
public class TransactionService {
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final TransactionQueue transactionQueue;

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(AccountRepository accountRepository,
                              AccountService accountService,
                              TransactionQueue transactionQueue) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.transactionQueue = transactionQueue;
    }


    /**
     * Processes a transaction request. If the transaction fails with a DatabaseUpdateException,
     * the transaction is automatically added to the retry queue (TransactionRetryScheduler will retry it).
     *
     * @param accountId id of the account to be updated
     * @param transactionRequest the transaction request to process
     * @param shouldRetry indicates if the transaction should go to a queue to be retried later on
     * @throws DatabaseUpdateException if a database error occurs during transaction processing (retryable)
     * @throws AccountNotFoundException if the account is not found
     */
    public Transaction addTransaction(String accountId, TransactionRequest transactionRequest, boolean shouldRetry) {
        try {
            return addTransactionInternal(accountId, transactionRequest);
        } catch (DatabaseUpdateException e) {
            logger.error("Transaction creation failed with database error for account: {}.", accountId, e);

            if(shouldRetry) {
                addTransactionToQueue(accountId, transactionRequest);
            }

            throw e;
        }
    }

    public TransactionHistoryResponse getTransactionHistory(String accountId, int page, int pageSize) {
        Account account = accountService.validateAccountExists(accountId);

        int safePage = Math.max(page, 0);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100); // cap at 100

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

    /**
     * Internal method to process the transaction logic.
     *
     * @param transactionRequest the transaction request to process
     * @throws AccountNotFoundException if the account is not found
     */
    private Transaction addTransactionInternal(String accountId, TransactionRequest transactionRequest) {
        Transaction transaction = buildTransaction(transactionRequest.getAmount(), transactionRequest.getTransactionType());

        accountRepository.updateAccount(accountId, account -> account.applyTransaction(transaction));

        return transaction;
    }

    private Transaction buildTransaction(BigDecimal amount, TransactionType transactionType) {
        return new Transaction(UUID.randomUUID().toString(), LocalDateTime.now(), amount, transactionType);
    }

    private void addTransactionToQueue(String accountId, TransactionRequest transactionRequest){
        FailedTransaction failedTransaction = new FailedTransaction(accountId, transactionRequest);
        transactionQueue.addToRetryQueue(failedTransaction);
    }
}
