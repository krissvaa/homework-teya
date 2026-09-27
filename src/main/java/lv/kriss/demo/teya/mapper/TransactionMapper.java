package lv.kriss.demo.teya.mapper;

import lv.kriss.demo.teya.domain.Balance;
import lv.kriss.demo.teya.domain.Money;
import lv.kriss.demo.teya.domain.Transaction;
import lv.kriss.demo.teya.dto.CreateTransactionRequest;
import lv.kriss.demo.teya.dto.TransactionDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new TransactionDto(
                transaction.getId(),
                transaction.getBalance().getId(),
                transaction.getAmount().getAmount(),
                transaction.getAmount().getCurrency().getCurrencyCode(),
                transaction.getType(),
                transaction.getBalanceAfter(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    public static List<TransactionDto> toDtoList(List<Transaction> transactions) {
        return transactions.stream()
                .map(TransactionMapper::toDto)
                .toList();
    }

    public static Transaction toEntity(CreateTransactionRequest request, Balance balance, BigDecimal balanceAfter) {
        var now = Instant.now();
        return new Transaction(
                null,
                balance,
                new Money(request.amount(), Currency.getInstance(request.currency())),
                request.type(),
                balanceAfter,
                now,
                now
        );
    }
}
