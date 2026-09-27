package lv.kriss.demo.teya.dto;

import java.util.List;
import java.util.UUID;

public record AccountDetailDto(
        UUID id,
        String name,
        List<BalanceDetailDto> balances
) {
}
