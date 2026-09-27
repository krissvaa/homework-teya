package lv.kriss.demo.teya.service;

import lv.kriss.demo.teya.domain.Account;
import lv.kriss.demo.teya.domain.Balance;
import lv.kriss.demo.teya.dto.BalanceDto;
import lv.kriss.demo.teya.dto.CreateBalanceRequest;
import lv.kriss.demo.teya.exception.DuplicateBalanceException;
import lv.kriss.demo.teya.exception.ResourceNotFoundException;
import lv.kriss.demo.teya.mapper.BalanceMapper;
import lv.kriss.demo.teya.repository.AccountRepository;
import lv.kriss.demo.teya.repository.BalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Service
public class BalanceService {
    private final Logger logger = LoggerFactory.getLogger(BalanceService.class);

    private final BalanceRepository balanceRepository;
    private final AccountRepository accountRepository;

    public BalanceService(BalanceRepository balanceRepository, AccountRepository accountRepository) {
        this.balanceRepository = balanceRepository;
        this.accountRepository = accountRepository;
    }

    public List<BalanceDto> getBalancesByAccountId(String accountId) {
        try {
            List<Balance> balances = balanceRepository.findByAccountId(UUID.fromString(accountId));
            return BalanceMapper.toDtoList(balances);
        } catch (IllegalArgumentException e) {
            logger.info("No account: {}", accountId, e);
            throw new ResourceNotFoundException("No account: " + accountId);
        }
    }

    public BalanceDto createBalance(CreateBalanceRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("No account: " + request.accountId()));

        var currency = Currency.getInstance(request.currency());
        if (balanceRepository.existsByAccountIdAndBalance_Currency(request.accountId(), currency)) {
            throw new DuplicateBalanceException(
                    String.format("Account %s already has a %s balance", request.accountId(), request.currency()));
        }

        Balance balance = balanceRepository.save(BalanceMapper.toEntity(request, account));
        return BalanceMapper.toDto(balance);
    }
}
