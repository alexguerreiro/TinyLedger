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
import java.util.Stack;

@Service
public class TransactionService {
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final TransactionRetryQueue transactionRetryQueue;
    private Stack<Transaction> transactionStack = new Stack<>();

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
        transactionStack.add(transaction);
        return addTransaction(accountId, transaction, true);
    }

    /**
     * Retry to add a transaction.
     *
     * @param accountId id of the account to be updated
     * @param transaction the transaction to process
     * @throws DatabaseUpdateException if a database error occurs during transaction processing (retryable)
     * @throws AccountNotFoundException if the account is not found
     * @throws InvalidTransactionException if the transaction is invalid (ex duplicated transaction)
     */
    public void retryAddTransaction(String accountId, Transaction transaction) {
        addTransaction(accountId, transaction, false);
    }

    // TODO this method was implemented during the code challenge
    // its not working as expected because its missing the part that updates the account in the repository
    public void rollback(String accountId) {
        Account account = accountRepository.getAccount(accountId);

        if(account == null){
            throw new AccountNotFoundException("Account not found for id: " + accountId);
        }

        while(!transactionStack.isEmpty()) {
            Transaction tx = transactionStack.pop();

            // this should work, however not fully tested
            Account rolledBackAccount = account.rollbackTransaction(tx);
            accountRepository.updateAccount(accountId, acc -> rolledBackAccount);
        }
    }

    private Transaction addTransaction(String accountId, Transaction transaction, boolean retry){
        try {
            accountRepository.updateAccount(accountId, account -> account.addTransaction(transaction));
            return transaction;
        } catch (DatabaseUpdateException e) {
            logger.error("Transaction creation failed with database error for account: {}.", accountId, e);

            if(retry) {
                addTransactionToRetryQueue(accountId, transaction);
            }

            throw new RetryableException("RetryableException occurred - transaction queued for retry", e);
        }
    }

    public TransactionHistoryResponse getTransactionHistory(String accountId, int page, int pageSize) {
        Account account = accountService.validateAccountExists(accountId);

        int safePage = Math.max(page, 0);
        int safePageSize = Math.clamp(pageSize, 1, 100);

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
