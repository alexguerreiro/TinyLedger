package com.teya.tinyledger.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(
        String id,
        LocalDateTime createdAt,
        BigDecimal amount,
        TransactionType transactionType
) {}

