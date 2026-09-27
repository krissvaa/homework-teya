package lv.kriss.demo.teya.controller;

import tools.jackson.databind.ObjectMapper;
import lv.kriss.demo.teya.domain.TransactionType;
import lv.kriss.demo.teya.dto.CreateTransactionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionControllerIntegrationTest {

    // Seed data: Alice's EUR balance, amount 175.50
    private static final UUID ALICE_EUR_BALANCE_ID = UUID.fromString("aaaaaaaa-1111-1111-1111-111111111111");
    // Seed data: Carol's EUR balance, amount 0.00
    private static final UUID CAROL_EUR_BALANCE_ID = UUID.fromString("cccccccc-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deposit_increasesBalanceAndRecordsTransaction() throws Exception {
        var request = new CreateTransactionRequest(
                ALICE_EUR_BALANCE_ID, new BigDecimal("50.00"), "EUR", TransactionType.DEPOSIT);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.balanceId").value(ALICE_EUR_BALANCE_ID.toString()))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.balanceAfter").value(225.50));
    }

    @Test
    void withdrawal_decreasesBalanceAndRecordsTransaction() throws Exception {
        var request = new CreateTransactionRequest(
                ALICE_EUR_BALANCE_ID, new BigDecimal("75.50"), "EUR", TransactionType.WITHDRAWAL);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.balanceAfter").value(100.00));
    }

    @Test
    void depositThenWithdrawal_transactionHistoryReflectsBothEntries() throws Exception {
        var deposit = new CreateTransactionRequest(
                CAROL_EUR_BALANCE_ID, new BigDecimal("100.00"), "EUR", TransactionType.DEPOSIT);
        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balanceAfter").value(100.00));

        var withdrawal = new CreateTransactionRequest(
                CAROL_EUR_BALANCE_ID, new BigDecimal("40.00"), "EUR", TransactionType.WITHDRAWAL);
        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(withdrawal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balanceAfter").value(60.00));

        mockMvc.perform(get("/transactions").param("balanceId", CAROL_EUR_BALANCE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"));
    }

    @Test
    void withdrawal_insufficientFunds_returnsBadRequestAndDoesNotChangeBalance() throws Exception {
        var request = new CreateTransactionRequest(
                CAROL_EUR_BALANCE_ID, new BigDecimal("0.01"), "EUR", TransactionType.WITHDRAWAL);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", not(blankOrNullString())));

        mockMvc.perform(get("/transactions").param("balanceId", CAROL_EUR_BALANCE_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void transaction_currencyMismatch_returnsBadRequest() throws Exception {
        var request = new CreateTransactionRequest(
                ALICE_EUR_BALANCE_ID, new BigDecimal("10.00"), "USD", TransactionType.DEPOSIT);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", not(blankOrNullString())));
    }

    @Test
    void transaction_unknownBalance_returnsNotFound() throws Exception {
        var request = new CreateTransactionRequest(
                UUID.fromString("99999999-9999-9999-9999-999999999999"),
                new BigDecimal("10.00"), "EUR", TransactionType.DEPOSIT);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void transaction_nonPositiveAmount_returnsBadRequest() throws Exception {
        var request = new CreateTransactionRequest(
                ALICE_EUR_BALANCE_ID, new BigDecimal("0"), "EUR", TransactionType.DEPOSIT);

        mockMvc.perform(post("/transactions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount", not(blankOrNullString())));
    }
}
