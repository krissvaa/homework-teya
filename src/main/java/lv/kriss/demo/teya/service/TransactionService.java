package lv.kriss.demo.teya.service;

import lv.kriss.demo.teya.dto.CreateTransactionRequest;
import lv.kriss.demo.teya.dto.TransactionDto;
import lv.kriss.demo.teya.exception.InvalidTransactionException;
import lv.kriss.demo.teya.exception.ResourceNotFoundException;
import lv.kriss.demo.teya.mapper.TransactionMapper;
import lv.kriss.demo.teya.repository.BalanceRepository;
import lv.kriss.demo.teya.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final BalanceRepository balanceRepository;

    public TransactionService(TransactionRepository transactionRepository, BalanceRepository balanceRepository) {
        this.transactionRepository = transactionRepository;
        this.balanceRepository = balanceRepository;
    }

    public List<TransactionDto> getTransactionsByBalanceId(String balanceId) {
        try {
            var transactions = transactionRepository.findByBalanceId(UUID.fromString(balanceId));
            return TransactionMapper.toDtoList(transactions);
        } catch (IllegalArgumentException e) {
            logger.info("No balance: {}", balanceId, e);
            throw new ResourceNotFoundException("No balance: " + balanceId);
        }
    }

    @Transactional
    public TransactionDto createTransaction(CreateTransactionRequest request) {
        var balance = balanceRepository.findById(request.balanceId())
                .orElseThrow(() -> new ResourceNotFoundException("No balance: " + request.balanceId()));

        var balanceCurrency = balance.getBalance().getCurrency().getCurrencyCode();
        if (!balanceCurrency.equals(request.currency())) {
            throw new InvalidTransactionException(
                    "Currency mismatch: balance " + balance.getId() + " is " + balanceCurrency
                            + ", transaction is " + request.currency());
        }

        BigDecimal newAmount = switch (request.type()) {
            case DEPOSIT -> balance.getBalance().getAmount().add(request.amount());
            case WITHDRAWAL -> balance.getBalance().getAmount().subtract(request.amount());
        };

        if (newAmount.signum() < 0) {
            throw new InvalidTransactionException("Insufficient funds on balance: " + balance.getId());
        }

        balance.getBalance().setAmount(newAmount);
        balance.setUpdatedAt(Instant.now());
        balanceRepository.save(balance);

        var transaction = transactionRepository.save(TransactionMapper.toEntity(request, balance, newAmount));
        return TransactionMapper.toDto(transaction);
    }
}
