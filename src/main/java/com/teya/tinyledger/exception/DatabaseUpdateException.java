package com.teya.tinyledger.exception;

/**
 * Exception thrown when a database update operation fails.
 * This exception is retryable - failed transactions will be added to the retry queue.
 * Typically thrown when there are transient database issues like connection timeouts.
 *
 * (Please note that this is a hypothecial exception for demonstration purposes, as we're using an in-memory data structure instead of a real database).
 */
public class DatabaseUpdateException extends RuntimeException {

    public DatabaseUpdateException(String message) {
        super(message);
    }

    public DatabaseUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}

