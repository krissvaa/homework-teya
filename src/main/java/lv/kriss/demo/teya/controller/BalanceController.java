package lv.kriss.demo.teya.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lv.kriss.demo.teya.dto.BalanceDto;
import lv.kriss.demo.teya.dto.CreateBalanceRequest;
import lv.kriss.demo.teya.service.BalanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/balances")
@Tag(name = "Balances")
@Validated
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping
    public List<BalanceDto> getBalances(@RequestParam @NotBlank String accountId) {
        return balanceService.getBalancesByAccountId(accountId);
    }

    @PostMapping
    public ResponseEntity<BalanceDto> createBalance(@Valid @RequestBody CreateBalanceRequest request) {
        BalanceDto created = balanceService.createBalance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
