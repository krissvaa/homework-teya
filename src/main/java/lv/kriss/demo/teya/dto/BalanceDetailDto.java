package lv.kriss.demo.teya.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BalanceDetailDto(
        UUID id,
        BigDecimal amount,
        String currency,
        List<TransactionDto> transactions
) {
}
