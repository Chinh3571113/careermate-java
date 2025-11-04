package com.fpt.careermate.unit.account_test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.account_services.service.AccountImp;
import com.fpt.careermate.services.account_services.service.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.account_services.service.dto.response.AccountResponse;
import com.fpt.careermate.services.account_services.web.rest.AccountController;
import com.fpt.careermate.services.email_services.service.EmailImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountTest {
    @Autowired
    private AccountController accountController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private AccountImp accountService;

    @MockitoBean
    private EmailImp emailService;

    public static final String END_POINT = "/api/users";
    private AccountCreationRequest request;
    private AccountResponse response;

    @BeforeEach
    void initData() {
        // Setup code if needed
        request = AccountCreationRequest.builder()
                .username("Nguyen Van A")
                .email("avan@gmail.com")
                .password("23102004Anh@")
                .status("ACTIVE")
                .build();
        response = AccountResponse.builder()
                .id(1)
                .username("Nguyen Van A")
                .email("")
                .status("ACTIVE")
                .build();
    }

    @Test
    @WithMockUser
    void createAccount_validInput_success() throws Exception {
        // GIVEN
        Mockito.when(accountService.createAccount(request)).thenReturn(response);
        // WHEN, THEN
        mockMvc.perform(
                MockMvcRequestBuilders.post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(0));
    }

    @Test
    @WithMockUser
    void createAccount_inValidInput_errorMessage() throws Exception {
        // GIVEN

        Mockito.when(accountService.createAccount(request)).thenReturn(response);
        // WHEN, THEN
        mockMvc.perform(
                        MockMvcRequestBuilders.post(END_POINT)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(0));
    }


}
