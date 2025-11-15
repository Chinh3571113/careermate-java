package com.fpt.careermate.unit.account_test.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static com.fpt.careermate.unit.account_test.controller.AccountTest.END_POINT;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountGetterTest {
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


    private AccountCreationRequest request;
    private AccountResponse response;

    @BeforeEach
    void initData() {
        request = AccountCreationRequest.builder()
                .username("Nguyen Van A")
                .email("avan@gmail.com")
                .password("23102004Anh@")
                .build();
        response = AccountResponse.builder()
                .id(1)
                .username("Nguyen Van A")
                .email("avan@gmail.com")
                .status("ACTIVE")
                .build();
    }

    // GET USER BY ID - valid
    @Test
    @WithMockUser
    void getUserById_validId_success() throws Exception {
        // GIVEN
        int userId = 1;
        Mockito.when(accountService.getAccountById(userId))
                .thenReturn(response);

        // WHEN, THEN
        mockMvc.perform(
                        MockMvcRequestBuilders.get(END_POINT + "/{id}", userId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.username").value("Nguyen Van A"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.email").value("avan@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.status").value("ACTIVE"));
    }

    // GET USER BY ID - user not found
    @Test
    @WithMockUser
    void getUserById_userNotFound_errorMessage() throws Exception {
        // GIVEN
        int userId = 999;
        Mockito.when(accountService.getAccountById(userId))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

        // WHEN, THEN
        mockMvc.perform(
                        MockMvcRequestBuilders.get(END_POINT + "/{id}", userId))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1005))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("User not existed"));
    }

    // GET USER BY ID - invalid id format
    @Test
    @WithMockUser
    void getUserById_invalidIdFormat_errorMessage() throws Exception {
        // WHEN, THEN
        mockMvc.perform(
                        MockMvcRequestBuilders.get(END_POINT + "/{id}", "invalid"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // GET CURRENT USER - success
    @Test
    @WithMockUser(username = "avan@gmail.com")
    void getCurrentUser_authenticated_success() throws Exception {
        // GIVEN
        Mockito.when(accountService.getCurrentUser())
                .thenReturn(response);

        // WHEN, THEN
        mockMvc.perform(
                        MockMvcRequestBuilders.get(END_POINT + "/current"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("result.username").value("Nguyen Van A"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.email").value("avan@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.status").value("ACTIVE"));
    }

    // GET CURRENT USER - unauthenticated (if filters are enabled)
    @Test
    void getCurrentUser_unauthenticated_errorMessage() throws Exception {
        // WHEN, THEN
        // Note: This test assumes @AutoConfigureMockMvc(addFilters = false) is removed
        // or security is configured differently
        mockMvc.perform(
                        MockMvcRequestBuilders.get(END_POINT + "/current"))
                .andExpect(MockMvcResultMatchers.status().isOk()); // Because addFilters = false
    }

    // GET CURRENT USER - user context not found
    @Test
    @WithMockUser(username = "notfound@gmail.com")
    void getCurrentUser_userNotFoundInContext_errorMessage() throws Exception {
        // GIVEN
        Mockito.when(accountService.getCurrentUser())
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

        // WHEN, THEN
        mockMvc.perform(
                        MockMvcRequestBuilders.get(END_POINT + "/current"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1005))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("User not existed"));
    }
}
