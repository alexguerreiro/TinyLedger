package com.teya.tinyledger.queue;

import com.teya.tinyledger.domain.FailedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Failed transactions are added to the queue for retry.
 * After 3 failed retries, transactions are moved to the dead letter queue (logic for handling the DLQ is not implemented)
 */
@Component
public class TransactionQueue {
    private static final Logger logger = LoggerFactory.getLogger(TransactionQueue.class);

    // TODO should be added to a property file
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final Queue<FailedTransaction> retryQueue = new ConcurrentLinkedQueue<>();
    private final Queue<FailedTransaction> deadLetterQueue = new ConcurrentLinkedQueue<>();

    /**
     * Adds a failed transaction to the retry queue.
     *
     * @param failedTransaction the failed transaction to add
     */
    public void addToRetryQueue(FailedTransaction failedTransaction) {
        retryQueue.add(failedTransaction);
    }

    /**
     * Retrieves and removes the next failed transaction from the retry queue.
     *
     * @return the next failed transaction or null if queue is empty
     */
    public FailedTransaction pollRetryQueue() {
        return retryQueue.poll();
    }

    /**
     * Returns the size of the retry queue.
     *
     * @return the number of transactions in the retry queue
     */
    public int getRetryQueueSize() {
        return retryQueue.size();
    }

    /**
     * Moves a failed transaction to the dead letter queue if it has exceeded max retry attempts.
     * Please note that The logic for handling the DLQ is not implemented
     *
     * @param failedTransaction the failed transaction
     * @return true if moved to dead letter queue, false if will be retried
     */
    public boolean moveToDeadLetterQueueIfMaxRetriesExceeded(FailedTransaction failedTransaction) {
        if (failedTransaction.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
            deadLetterQueue.add(failedTransaction);
            logger.warn("Transaction moved to dead letter queue after {} failed attempts for account: {}",
                MAX_RETRY_ATTEMPTS, failedTransaction.getTransactionRequest().getAccountId());
            return true;
        }
        return false;
    }
}

