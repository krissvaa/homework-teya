package lv.kriss.demo.teya.controller;

import lv.kriss.demo.teya.dto.TransactionDto;
import lv.kriss.demo.teya.service.TransactionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionDto> getTransactions(@RequestParam String balanceId) {
        return transactionService.getTransactionsByBalanceId(balanceId);
    }
}
