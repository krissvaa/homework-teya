package lv.kriss.demo.teya.service;

import lv.kriss.demo.teya.domain.Balance;
import lv.kriss.demo.teya.dto.AccountDetailDto;
import lv.kriss.demo.teya.dto.AccountDto;
import lv.kriss.demo.teya.dto.CreateAccountRequest;
import lv.kriss.demo.teya.exception.ResourceNotFoundException;
import lv.kriss.demo.teya.mapper.AccountMapper;
import lv.kriss.demo.teya.mapper.BalanceMapper;
import lv.kriss.demo.teya.repository.AccountRepository;
import lv.kriss.demo.teya.repository.BalanceRepository;
import lv.kriss.demo.teya.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, BalanceRepository balanceRepository,
                           TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.transactionRepository = transactionRepository;
    }

    public AccountDto createAccount(CreateAccountRequest request) {
        var account = accountRepository.save(AccountMapper.toEntity(request));
        return AccountMapper.toDto(account);
    }

    public List<AccountDto> getAllAccounts() {
        var accounts = accountRepository.findAll();
        return AccountMapper.toDtoList(accounts);
    }

    public AccountDetailDto getAccountDetail(String id) {
        UUID accountId;
        try {
            accountId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("No account: " + id);
        }

        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account: " + id));

        var balances = balanceRepository.findByAccountId(accountId);
        var balanceIds = balances.stream().map(Balance::getId).toList();
        var transactionsByBalanceId = transactionRepository.findByBalanceIdIn(balanceIds).stream()
                .collect(Collectors.groupingBy(t -> t.getBalance().getId()));

        var balanceDetails = balances.stream()
                .map(b -> BalanceMapper.toDetailDto(b, transactionsByBalanceId.getOrDefault(b.getId(), List.of())))
                .toList();

        return AccountMapper.toDetailDto(account, balanceDetails);
    }
}
