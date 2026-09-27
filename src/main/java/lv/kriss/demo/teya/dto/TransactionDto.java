package lv.kriss.demo.teya.dto;

import lv.kriss.demo.teya.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionDto(
        UUID id,
        UUID balanceId,
        BigDecimal amount,
        String currency,
        TransactionType type,
        BigDecimal balanceAfter,
        Instant createdAt,
        Instant updatedAt
) {
}
