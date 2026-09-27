package lv.kriss.demo.teya.service;

import lv.kriss.demo.teya.dto.BalanceDto;
import lv.kriss.demo.teya.exception.ResourceNotFoundException;
import lv.kriss.demo.teya.mapper.BalanceMapper;
import lv.kriss.demo.teya.repository.BalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BalanceService {
    private final Logger logger = LoggerFactory.getLogger(BalanceService.class);

    private final BalanceRepository balanceRepository;

    public BalanceService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public List<BalanceDto> getBalancesByAccountId(String accountId) {
        try {
            var balances = balanceRepository.findByAccountId(UUID.fromString(accountId));
            return BalanceMapper.toDtoList(balances);
        } catch (IllegalArgumentException e) {
            logger.info("No account: {}", accountId, e);
            throw new ResourceNotFoundException("No account: " + accountId);
        }
    }
}
