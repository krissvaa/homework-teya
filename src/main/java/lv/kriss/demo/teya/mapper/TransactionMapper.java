package lv.kriss.demo.teya.mapper;

import lv.kriss.demo.teya.domain.Transaction;
import lv.kriss.demo.teya.dto.TransactionDto;

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
}
