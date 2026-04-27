package com.teya.tinyledger.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record Transaction(
        String id,
        LocalDateTime createdAt,
        BigDecimal amount,
        TransactionType transactionType
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

