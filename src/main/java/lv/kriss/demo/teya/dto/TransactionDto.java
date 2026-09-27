package lv.kriss.demo.teya.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lv.kriss.demo.teya.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionDto(
        UUID id,
        @NotNull UUID balanceId,
        @NotNull @Positive BigDecimal amount,
        @NotNull @Pattern(regexp = "^[A-Z]{3}$", message = "must be a 3-letter ISO 4217 currency code") String currency,
        @NotNull TransactionType type,
        @NotNull @PositiveOrZero BigDecimal balanceAfter,
        Instant createdAt
) {
}
