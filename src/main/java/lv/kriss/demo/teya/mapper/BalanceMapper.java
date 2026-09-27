package lv.kriss.demo.teya.mapper;

import lv.kriss.demo.teya.domain.Account;
import lv.kriss.demo.teya.domain.Balance;
import lv.kriss.demo.teya.domain.Money;
import lv.kriss.demo.teya.domain.Transaction;
import lv.kriss.demo.teya.dto.BalanceDetailDto;
import lv.kriss.demo.teya.dto.BalanceDto;
import lv.kriss.demo.teya.dto.CreateBalanceRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

public final class BalanceMapper {

    private BalanceMapper() {
    }

    public static BalanceDto toDto(Balance balance) {
        if (balance == null) {
            return null;
        }
        Currency currency = balance.getBalance().getCurrency();
        return new BalanceDto(
                balance.getId(),
                balance.getAccount().getId(),
                Money.scale(balance.getBalance().getAmount(), currency),
                currency.getCurrencyCode(),
                balance.getCreatedAt(),
                balance.getUpdatedAt()
        );
    }

    public static List<BalanceDto> toDtoList(List<Balance> balances) {
        return balances.stream()
                .map(BalanceMapper::toDto)
                .toList();
    }

    public static Balance toEntity(CreateBalanceRequest request, Account account) {
        var now = Instant.now();
        return new Balance(
                null,
                Money.of(BigDecimal.ZERO, Currency.getInstance(request.currency())),
                account,
                now,
                now,
                null
        );
    }

    public static BalanceDetailDto toDetailDto(Balance balance, List<Transaction> transactions) {
        Currency currency = balance.getBalance().getCurrency();
        return new BalanceDetailDto(
                balance.getId(),
                Money.scale(balance.getBalance().getAmount(), currency),
                currency.getCurrencyCode(),
                TransactionMapper.toDtoList(transactions)
        );
    }
}
