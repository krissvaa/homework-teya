package lv.kriss.demo.teya.controller;

import tools.jackson.databind.ObjectMapper;
import lv.kriss.demo.teya.dto.CreateAccountRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAccount_persistsAndReturnsAccount() throws Exception {
        var request = new CreateAccountRequest("Dana Prince");

        mockMvc.perform(post("/accounts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(blankOrNullString())))
                .andExpect(jsonPath("$.name").value("Dana Prince"))
                .andExpect(jsonPath("$.createdAt", not(blankOrNullString())))
                .andExpect(jsonPath("$.updatedAt", not(blankOrNullString())));
    }

    @Test
    void createAccount_appearsInAccountList() throws Exception {
        var request = new CreateAccountRequest("Erin Walsh");

        var responseBody = mockMvc.perform(post("/accounts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var createdId = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(get("/accounts/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.name").value("Erin Walsh"))
                .andExpect(jsonPath("$.balances").isArray())
                .andExpect(jsonPath("$.balances").isEmpty());
    }

    @Test
    void createAccount_blankName_returnsBadRequest() throws Exception {
        var request = new CreateAccountRequest(" ");

        mockMvc.perform(post("/accounts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name", not(blankOrNullString())));
    }

    @Test
    void getAccountDetail_withKnownSeedAccount_returnsBalancesAndTransactions() throws Exception {
        mockMvc.perform(get("/accounts/{id}", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.balances[0].currency").value("EUR"))
                .andExpect(jsonPath("$.balances[0].transactions").isNotEmpty());
    }

    @Test
    void getAccountDetail_unknownId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/accounts/{id}", "99999999-9999-9999-9999-999999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAccountDetail_malformedId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/accounts/{id}", "not-a-uuid"))
                .andExpect(status().isNotFound());
    }
}
