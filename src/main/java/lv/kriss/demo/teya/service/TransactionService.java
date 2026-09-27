package lv.kriss.demo.teya.service;

import lv.kriss.demo.teya.dto.TransactionDto;
import lv.kriss.demo.teya.exception.ResourceNotFoundException;
import lv.kriss.demo.teya.mapper.TransactionMapper;
import lv.kriss.demo.teya.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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
}
