package lv.kriss.demo.teya.service;

import lv.kriss.demo.teya.dto.BalanceDto;
import lv.kriss.demo.teya.mapper.BalanceMapper;
import lv.kriss.demo.teya.repository.BalanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BalanceService {

    private final BalanceRepository balanceRepository;

    public BalanceService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public List<BalanceDto> getBalancesByAccountId(String accountId) {
        var balances = balanceRepository.findByAccountId(UUID.fromString(accountId));
        return BalanceMapper.toDtoList(balances);
    }
}
