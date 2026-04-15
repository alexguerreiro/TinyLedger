package com.teya.tinyledger.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
        UUID id,
        LocalDateTime createdAt,
        Double amount,
        OperationType operationType
) {}

