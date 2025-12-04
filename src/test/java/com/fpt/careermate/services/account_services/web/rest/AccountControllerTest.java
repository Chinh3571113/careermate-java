package com.fpt.careermate.services.account_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.common.util.ChangePassword;
import com.fpt.careermate.services.account_services.service.AccountImp;
import com.fpt.careermate.services.account_services.service.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.account_services.service.dto.request.SignUpRequest;
import com.fpt.careermate.services.account_services.service.dto.response.AccountResponse;
import com.fpt.careermate.services.email_services.service.EmailImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for AccountController
 * Function Code: ACC-CTRL-001 to ACC-CTRL-010
 * 
 * Test Matrix:
 * - TC001: Create account with valid data -> 200 OK
 * - TC002: Create account with duplicate email -> 409 Conflict
 * - TC003: Get all accounts -> 200 OK with paginated list
 * - TC004: Get account by ID (exists) -> 200 OK
 * - TC005: Get account by ID (not exists) -> 404 Not Found
 * - TC006: Delete account -> 200 OK
 * - TC007: Update account status -> 200 OK
 * - TC008: Sign up new user -> 200 OK
 * - TC009: Verify email -> 200 OK
 * - TC010: Change password -> 200 OK
 */
@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AccountController Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountImp accountImp;

    @MockBean
    private EmailImp emailImp;

    private AccountResponse testAccountResponse;
    private AccountCreationRequest validCreationRequest;

    @BeforeEach
    void setUp() {
        testAccountResponse = AccountResponse.builder()
                .id(1)
                .email("test@example.com")
                .username("Test User")
                .build();

        validCreationRequest = AccountCreationRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .username("New User")
                .build();
    }

    @Nested
    @DisplayName("POST /api/users - Create Account")
    class CreateAccountTests {

        @Test
        @DisplayName("TC001: Create account with valid data returns 200 OK")
        void createAccount_WithValidData_Returns200() throws Exception {
            // Given - Precondition: Email does not exist
            when(accountImp.createAccount(any(AccountCreationRequest.class)))
                    .thenReturn(testAccountResponse);

            // When - Input: valid account data
            ResultActions result = mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // Then - Expected: 200 OK with account response
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value(1))
                    .andExpect(jsonPath("$.result.email").value("test@example.com"));
        }

        @Test
        @DisplayName("TC002: Create account with duplicate email returns 400 Bad Request")
        void createAccount_WithDuplicateEmail_Returns400() throws Exception {
            // Given - Precondition: Email already exists
            when(accountImp.createAccount(any(AccountCreationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.USER_EXISTED));

            // When - Input: existing email
            ResultActions result = mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreationRequest)));

            // Then - Expected: 400 Bad Request
            result.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC003: Create account with invalid email format returns 400")
        void createAccount_WithInvalidEmail_Returns400() throws Exception {
            // Given - Precondition: Invalid email format
            AccountCreationRequest invalidRequest = AccountCreationRequest.builder()
                    .email("invalid-email")
                    .password("Password123!")
                    .username("Test")
                    .build();

            // When - Input: invalid email format
            ResultActions result = mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)));

            // Then - Expected: 400 Bad Request
            result.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/users/all - Get All Accounts")
    class GetAllAccountsTests {

        @Test
        @DisplayName("TC004: Get all accounts returns 200 OK with paginated list")
        void getAllAccounts_Returns200WithList() throws Exception {
            // Given - Precondition: Accounts exist
            PageResponse<AccountResponse> pageResponse = new PageResponse<>(
                    List.of(testAccountResponse), 0, 10, 1L, 1);

            when(accountImp.searchAccounts(any(), any(), any(), any()))
                    .thenReturn(pageResponse);

            // When - Input: pagination params
            ResultActions result = mockMvc.perform(get("/api/users/all")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK with paginated response
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.content").isArray());
        }

        @Test
        @DisplayName("TC005: Get accounts with role filter returns filtered list")
        void getAllAccounts_WithRoleFilter_ReturnsFilteredList() throws Exception {
            // Given - Precondition: Accounts with role exist
            PageResponse<AccountResponse> pageResponse = new PageResponse<>(
                    List.of(testAccountResponse), 0, 10, 1L, 1);

            when(accountImp.searchAccounts(any(), any(), any(), any()))
                    .thenReturn(pageResponse);

            // When - Input: role filter
            ResultActions result = mockMvc.perform(get("/api/users/all")
                    .param("roles", "ROLE_CANDIDATE")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK with filtered response
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id} - Get Account by ID")
    class GetAccountByIdTests {

        @Test
        @DisplayName("TC006: Get existing account returns 200 OK")
        void getAccountById_Exists_Returns200() throws Exception {
            // Given - Precondition: Account exists
            when(accountImp.getAccountById(1)).thenReturn(testAccountResponse);

            // When - Input: valid account ID
            ResultActions result = mockMvc.perform(get("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK with account data
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value(1))
                    .andExpect(jsonPath("$.result.email").value("test@example.com"));
        }

        @Test
        @DisplayName("TC007: Get non-existent account returns 404 Not Found")
        void getAccountById_NotExists_Returns404() throws Exception {
            // Given - Precondition: Account does not exist
            when(accountImp.getAccountById(999))
                    .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

            // When - Input: non-existent ID
            ResultActions result = mockMvc.perform(get("/api/users/999")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 404 Not Found
            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/users/current - Get Current User")
    class GetCurrentUserTests {

        @Test
        @DisplayName("TC008: Get current user returns 200 OK")
        void getCurrentUser_Authenticated_Returns200() throws Exception {
            // Given - Precondition: User is authenticated
            when(accountImp.getCurrentUser()).thenReturn(testAccountResponse);

            // When - Input: authenticated request
            ResultActions result = mockMvc.perform(get("/api/users/current")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK with user data
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.email").value("test@example.com"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id} - Delete Account")
    class DeleteAccountTests {

        @Test
        @DisplayName("TC009: Delete existing account returns 200 OK")
        void deleteAccount_Exists_Returns200() throws Exception {
            // Given - Precondition: Account exists
            doNothing().when(accountImp).deleteAccount(1);

            // When - Input: valid account ID
            ResultActions result = mockMvc.perform(delete("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK with success message
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Delete account successfully"));
        }

        @Test
        @DisplayName("TC010: Delete non-existent account returns 404")
        void deleteAccount_NotExists_Returns404() throws Exception {
            // Given - Precondition: Account does not exist
            when(accountImp.getAccountById(999))
                    .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

            // When - Input: non-existent ID (mock deleteAccount to throw)
            org.mockito.Mockito.doThrow(new AppException(ErrorCode.USER_NOT_EXISTED))
                    .when(accountImp).deleteAccount(999);

            ResultActions result = mockMvc.perform(delete("/api/users/999")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 404 Not Found
            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}/status - Update Account Status")
    class UpdateStatusTests {

        @Test
        @DisplayName("TC011: Update status to ACTIVE returns 200 OK")
        void updateStatus_ToActive_Returns200() throws Exception {
            // Given - Precondition: Account exists
            AccountResponse updatedAccount = AccountResponse.builder()
                    .id(1)
                    .email("test@example.com")
                    .username("Test User")
                    .build();

            when(accountImp.updateAccountStatus(eq(1), eq("ACTIVE")))
                    .thenReturn(updatedAccount);

            // When - Input: valid ID and status
            ResultActions result = mockMvc.perform(put("/api/users/1/status")
                    .param("status", "ACTIVE")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK with updated account
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Account status updated successfully"));
        }

        @Test
        @DisplayName("TC012: Update status with invalid status returns 400")
        void updateStatus_InvalidStatus_Returns400() throws Exception {
            // Given - Precondition: Invalid status value
            when(accountImp.updateAccountStatus(eq(1), eq("INVALID")))
                    .thenThrow(new AppException(ErrorCode.INVALID_STATUS));

            // When - Input: invalid status
            ResultActions result = mockMvc.perform(put("/api/users/1/status")
                    .param("status", "INVALID")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 400 Bad Request
            result.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/users/sign-up - Sign Up")
    class SignUpTests {

        @Test
        @DisplayName("TC013: Sign up with valid data returns 200 OK")
        void signUp_WithValidData_Returns200() throws Exception {
            // Given - Precondition: Email does not exist
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .email("newuser@example.com")
                    .password("Password123!")
                    .fullName("New User")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .build();

            doNothing().when(accountImp).signUp(any(SignUpRequest.class));

            // When - Input: valid sign up data
            ResultActions result = mockMvc.perform(post("/api/users/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)));

            // Then - Expected: 200 OK with success message
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Sign up successful"));
        }

        @Test
        @DisplayName("TC014: Sign up with existing email returns 400 Bad Request")
        void signUp_WithExistingEmail_Returns400() throws Exception {
            // Given - Precondition: Email already exists
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .email("existing@example.com")
                    .password("Password123!")
                    .fullName("New User")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .build();

            org.mockito.Mockito.doThrow(new AppException(ErrorCode.USER_EXISTED))
                    .when(accountImp).signUp(any(SignUpRequest.class));

            // When - Input: existing email
            ResultActions result = mockMvc.perform(post("/api/users/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)));

            // Then - Expected: 400 Bad Request
            result.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Email Verification and Password Reset")
    class EmailVerificationTests {

        @Test
        @DisplayName("TC015: Verify email sends OTP returns 200 OK")
        void verifyEmail_ValidEmail_Returns200() throws Exception {
            // Given - Precondition: Email exists
            when(emailImp.verifyEmail("test@example.com"))
                    .thenReturn("OTP sent successfully");

            // When - Input: valid email
            ResultActions result = mockMvc.perform(post("/api/users/verify-email/test@example.com")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK
            result.andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC016: Verify OTP with valid code returns 200 OK")
        void verifyOtp_ValidCode_Returns200() throws Exception {
            // Given - Precondition: OTP is valid
            when(emailImp.verifyOtp(eq("test@example.com"), eq(123456)))
                    .thenReturn("token-123");

            // When - Input: valid email and OTP
            ResultActions result = mockMvc.perform(post("/api/users/verify-otp")
                    .param("email", "test@example.com")
                    .param("code", "123456")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK with token
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("token-123"));
        }

        @Test
        @DisplayName("TC017: Change password with valid token returns 200 OK")
        void changePassword_ValidToken_Returns200() throws Exception {
            // Given - Precondition: Valid reset token
            ChangePassword changePassword = new ChangePassword("NewPassword123!", "NewPassword123!");

            when(emailImp.changePassword(any(ChangePassword.class), eq("test@example.com")))
                    .thenReturn("Password changed successfully");

            // When - Input: valid password change request
            ResultActions result = mockMvc.perform(put("/api/users/change-password/test@example.com")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(changePassword)));

            // Then - Expected: 200 OK
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password changed successfully"));
        }
    }
}
