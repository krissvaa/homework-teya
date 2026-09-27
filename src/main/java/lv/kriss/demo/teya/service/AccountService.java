package lv.kriss.demo.teya.service;

import lv.kriss.demo.teya.dto.AccountDto;
import lv.kriss.demo.teya.mapper.AccountMapper;
import lv.kriss.demo.teya.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void createAccount() {

    }

    public List<AccountDto> getAllAccounts() {
        var accounts = accountRepository.findAll();
        return AccountMapper.toDtoList(accounts);
    }
}
