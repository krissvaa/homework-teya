package lv.kriss.demo.teya.controller;


import lv.kriss.demo.teya.dto.AccountDto;
import lv.kriss.demo.teya.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountDto> getAllUsers() {
        return accountService.getAllAccounts();
    }
}
