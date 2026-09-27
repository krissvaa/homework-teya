package lv.kriss.demo.teya.controller;

import lv.kriss.demo.teya.dto.BalanceDto;
import lv.kriss.demo.teya.service.BalanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping
    public List<BalanceDto> getBalances(@RequestParam String accountId) {
        return balanceService.getBalancesByAccountId(accountId);
    }
}
