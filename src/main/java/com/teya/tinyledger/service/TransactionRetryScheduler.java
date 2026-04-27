package com.teya.tinyledger.service;

import com.teya.tinyledger.domain.FailedTransaction;
import com.teya.tinyledger.exception.RetryableException;
import com.teya.tinyledger.queue.TransactionRetryQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service that retries failed transactions from the retry queue every minute.
 * If a transaction fails after 3 retry attempts, it is moved to the dead letter queue.
 */
@Service
@EnableScheduling
public class TransactionRetryScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRetryScheduler.class);

    private final TransactionRetryQueue transactionRetryQueue;
    private final TransactionService transactionService;

    public TransactionRetryScheduler(TransactionRetryQueue transactionRetryQueue, TransactionService transactionService) {
        this.transactionRetryQueue = transactionRetryQueue;
        this.transactionService = transactionService;
    }

    /**
     * Runs every minute (60000 milliseconds) to process failed transactions from the retry queue.
     * Each failed transaction is retried, and if it fails again, the retry count is incremented.
     * After 3 failed retries, the transaction is moved to the dead letter queue.
     */
    @Scheduled(fixedRate = 60000)
    public void retryFailedTransactions() {
        int queueSize = transactionRetryQueue.getRetryQueueSize();

        if (queueSize == 0) {
            logger.debug("No transactions to retry in the queue");
            return;
        }

        FailedTransaction failedTransaction;
        while ((failedTransaction = transactionRetryQueue.pollRetryQueue()) != null) {
            try {
                transactionService.retryAddTransaction(failedTransaction.getAccountId(), failedTransaction.getTransaction());
            } catch (RetryableException e) {
                logger.error("Transaction retry failed for transaction id: {}", failedTransaction.getTransaction().id(), e);

                failedTransaction.incrementRetryCount();

                // Check if we should move to dead letter queue
                if (!transactionRetryQueue.moveToDeadLetterQueueIfMaxRetriesExceeded(failedTransaction)) {
                    // Put back in retry queue if not exceeded max retries
                    transactionRetryQueue.addToRetryQueue(failedTransaction);
                }
            } catch (Exception e) {
                logger.error("Transaction retry failed for transaction id: {}", failedTransaction.getTransaction().id(), e);
            }
        }
    }
}
