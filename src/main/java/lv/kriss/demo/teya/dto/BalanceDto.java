package lv.kriss.demo.teya.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BalanceDto(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {
}
