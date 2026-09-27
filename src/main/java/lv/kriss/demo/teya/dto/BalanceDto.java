package lv.kriss.demo.teya.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BalanceDto(
        UUID id,
        @NotNull UUID accountId,
        @NotNull @PositiveOrZero BigDecimal amount,
        @NotNull @Pattern(regexp = "^[A-Z]{3}$", message = "must be a 3-letter ISO 4217 currency code") String currency,
        Instant createdAt,
        Instant updatedAt
) {
}
