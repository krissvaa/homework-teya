package lv.kriss.demo.teya.controller;

import tools.jackson.databind.ObjectMapper;
import lv.kriss.demo.teya.dto.CreateBalanceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class BalanceControllerIntegrationTest {

    private static final UUID ALICE_ACCOUNT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBalance_newCurrencyForAccount_persistsWithZeroAmount() throws Exception {
        // Alice already has a EUR balance (seed data), so add a new USD balance.
        var request = new CreateBalanceRequest(ALICE_ACCOUNT_ID, "USD");

        var responseBody = mockMvc.perform(post("/balances")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.accountId").value(ALICE_ACCOUNT_ID.toString()))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(0))
                .andReturn().getResponse().getContentAsString();

        var createdId = objectMapper.readTree(responseBody).get("id").asString();

        mockMvc.perform(get("/balances").param("accountId", ALICE_ACCOUNT_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + createdId + "')].currency").value("USD"));
    }

    @Test
    void createBalance_duplicateCurrencyForAccount_returnsConflict() throws Exception {
        // Alice already has a EUR balance from seed data.
        var request = new CreateBalanceRequest(ALICE_ACCOUNT_ID, "EUR");

        mockMvc.perform(post("/balances")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", not(blankOrNullString())));
    }

    @Test
    void createBalance_unknownAccount_returnsNotFound() throws Exception {
        var request = new CreateBalanceRequest(UUID.fromString("99999999-9999-9999-9999-999999999999"), "USD");

        mockMvc.perform(post("/balances")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBalance_invalidCurrencyCode_returnsBadRequest() throws Exception {
        var request = new CreateBalanceRequest(ALICE_ACCOUNT_ID, "usd");

        mockMvc.perform(post("/balances")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.currency", not(blankOrNullString())));
    }

    @Test
    void createBalance_wellFormedButUnknownCurrencyCode_returnsBadRequest() throws Exception {
        var request = new CreateBalanceRequest(ALICE_ACCOUNT_ID, "ABC");

        mockMvc.perform(post("/balances")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", not(blankOrNullString())));
    }

    @Test
    void getBalances_byAccountId_returnsSeedBalance() throws Exception {
        mockMvc.perform(get("/balances").param("accountId", ALICE_ACCOUNT_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currency").value("EUR"))
                .andExpect(jsonPath("$[0].amount").value(175.50));
    }

    @Test
    void getBalances_blankAccountId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/balances").param("accountId", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.accountId", not(blankOrNullString())));
    }
}
