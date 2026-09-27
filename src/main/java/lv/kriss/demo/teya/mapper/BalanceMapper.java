package lv.kriss.demo.teya.mapper;

import lv.kriss.demo.teya.domain.Balance;
import lv.kriss.demo.teya.dto.BalanceDto;

import java.util.List;

public final class BalanceMapper {

    private BalanceMapper() {
    }

    public static BalanceDto toDto(Balance balance) {
        if (balance == null) {
            return null;
        }
        return new BalanceDto(
                balance.getId(),
                balance.getAccount().getId(),
                balance.getBalance().getAmount(),
                balance.getBalance().getCurrency().getCurrencyCode(),
                balance.getCreatedAt(),
                balance.getUpdatedAt()
        );
    }

    public static List<BalanceDto> toDtoList(List<Balance> balances) {
        return balances.stream()
                .map(BalanceMapper::toDto)
                .toList();
    }
}
