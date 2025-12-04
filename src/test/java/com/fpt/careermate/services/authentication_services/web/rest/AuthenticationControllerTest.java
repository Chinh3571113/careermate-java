package com.fpt.careermate.services.authentication_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.authentication_services.service.dto.request.AuthenticationRequest;
import com.fpt.careermate.services.authentication_services.service.dto.request.IntrospectRequest;
import com.fpt.careermate.services.authentication_services.service.dto.response.AuthenticationResponse;
import com.fpt.careermate.services.authentication_services.service.dto.response.IntrospectResponse;
import com.fpt.careermate.services.authentication_services.service.dto.response.MobileAuthenticationResponse;
import jakarta.servlet.http.Cookie;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for AuthenticationController
 * Function Code: AUTH-CTRL-001 to AUTH-CTRL-006
 * 
 * Test Matrix:
 * - TC001: Login with valid credentials -> 200 OK with token
 * - TC002: Login with invalid password -> 401 Unauthorized
 * - TC003: Login with non-existent user -> 404 Not Found
 * - TC004: Refresh token successfully -> 200 OK with new token
 * - TC005: Refresh with invalid token -> 401 Unauthorized
 * - TC006: Introspect valid token -> 200 OK with valid=true
 * - TC007: Introspect invalid token -> 200 OK with valid=false
 * - TC008: Mobile login success -> 200 OK with tokens in body
 */
@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthenticationController Tests")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationImp authenticationImp;

    private AuthenticationRequest validLoginRequest;
    private AuthenticationResponse successResponse;

    @BeforeEach
    void setUp() {
        validLoginRequest = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        successResponse = AuthenticationResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlRlc3QgVXNlciIsImlhdCI6MTUxNjIzOTAyMn0")
                .refreshToken("refresh-token-123")
                .authenticated(true)
                .expiresIn(3600L)
                .tokenType("Bearer")
                .build();
    }

    @Nested
    @DisplayName("POST /api/auth/token - Login")
    class LoginTests {

        @Test
        @DisplayName("TC001: Login with valid credentials returns 200 OK with token")
        void login_WithValidCredentials_Returns200WithToken() throws Exception {
            // Given - Precondition: Valid user exists
            when(authenticationImp.authenticate(any(AuthenticationRequest.class)))
                    .thenReturn(successResponse);

            // When - Input: valid email and password
            ResultActions result = mockMvc.perform(post("/api/auth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)));

            // Then - Expected: 200 OK with access token
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.accessToken").exists())
                    .andExpect(jsonPath("$.result.authenticated").value(true))
                    .andExpect(jsonPath("$.result.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("TC002: Login with invalid password returns 401 Unauthorized")
        void login_WithInvalidPassword_Returns401() throws Exception {
            // Given - Precondition: User exists but wrong password
            when(authenticationImp.authenticate(any(AuthenticationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

            AuthenticationRequest invalidRequest = AuthenticationRequest.builder()
                    .email("test@example.com")
                    .password("WrongPassword")
                    .build();

            // When - Input: valid email, wrong password
            ResultActions result = mockMvc.perform(post("/api/auth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)));

            // Then - Expected: 401 Unauthorized
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("TC003: Login with non-existent user returns 404 Not Found")
        void login_WithNonExistentUser_Returns404() throws Exception {
            // Given - Precondition: User does not exist
            when(authenticationImp.authenticate(any(AuthenticationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

            AuthenticationRequest invalidRequest = AuthenticationRequest.builder()
                    .email("nonexistent@example.com")
                    .password("Password123!")
                    .build();

            // When - Input: non-existent email
            ResultActions result = mockMvc.perform(post("/api/auth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)));

            // Then - Expected: 404 Not Found
            result.andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC004: Login with empty email returns 400 Bad Request")
        void login_WithEmptyEmail_Returns400() throws Exception {
            // Given - Precondition: Invalid input
            AuthenticationRequest invalidRequest = AuthenticationRequest.builder()
                    .email("")
                    .password("Password123!")
                    .build();

            // When - Input: empty email
            ResultActions result = mockMvc.perform(post("/api/auth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)));

            // Then - Expected: 400 Bad Request
            result.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC005: Login with null password returns 400 Bad Request")
        void login_WithNullPassword_Returns400() throws Exception {
            // Given - Precondition: Invalid input
            AuthenticationRequest invalidRequest = AuthenticationRequest.builder()
                    .email("test@example.com")
                    .password(null)
                    .build();

            // When - Input: null password
            ResultActions result = mockMvc.perform(post("/api/auth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)));

            // Then - Expected: 400 Bad Request
            result.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/auth/refresh - Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("TC006: Refresh token successfully returns 200 OK with new token")
        void refreshToken_WithValidToken_Returns200WithNewToken() throws Exception {
            // Given - Precondition: Valid refresh token in cookie
            when(authenticationImp.refreshToken(any()))
                    .thenReturn(successResponse);

            Cookie refreshTokenCookie = new Cookie("refreshToken", "valid-refresh-token");

            // When - Input: valid refresh token cookie
            ResultActions result = mockMvc.perform(put("/api/auth/refresh")
                    .cookie(refreshTokenCookie)
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK with new access token
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.accessToken").exists())
                    .andExpect(jsonPath("$.result.authenticated").value(true));
        }

        @Test
        @DisplayName("TC007: Refresh with no cookie returns 401 Unauthorized")
        void refreshToken_WithNoCookie_Returns401() throws Exception {
            // Given - Precondition: No refresh token cookie

            // When - Input: no cookie
            ResultActions result = mockMvc.perform(put("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 401 Unauthorized
            result.andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/auth/introspect - Token Introspection")
    class IntrospectTests {

        @Test
        @DisplayName("TC008: Introspect valid token returns valid=true")
        void introspect_WithValidToken_ReturnsValidTrue() throws Exception {
            // Given - Precondition: Token is valid
            IntrospectResponse validResponse = IntrospectResponse.builder()
                    .valid(true)
                    .build();

            when(authenticationImp.introspect(any(IntrospectRequest.class)))
                    .thenReturn(validResponse);

            IntrospectRequest request = IntrospectRequest.builder()
                    .token("valid-token")
                    .build();

            // When - Input: valid token
            ResultActions result = mockMvc.perform(put("/api/auth/introspect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then - Expected: 200 OK with valid=true
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.valid").value(true));
        }

        @Test
        @DisplayName("TC009: Introspect invalid token returns valid=false")
        void introspect_WithInvalidToken_ReturnsValidFalse() throws Exception {
            // Given - Precondition: Token is invalid/expired
            IntrospectResponse invalidResponse = IntrospectResponse.builder()
                    .valid(false)
                    .build();

            when(authenticationImp.introspect(any(IntrospectRequest.class)))
                    .thenReturn(invalidResponse);

            IntrospectRequest request = IntrospectRequest.builder()
                    .token("invalid-token")
                    .build();

            // When - Input: invalid token
            ResultActions result = mockMvc.perform(put("/api/auth/introspect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Then - Expected: 200 OK with valid=false
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.valid").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/token/candidate - Mobile Login")
    class MobileLoginTests {

        @Test
        @DisplayName("TC010: Mobile login success returns tokens in response body")
        void mobileLogin_WithValidCredentials_ReturnsTokensInBody() throws Exception {
            // Given - Precondition: Valid candidate user
            when(authenticationImp.authenticateCandidate(any(AuthenticationRequest.class)))
                    .thenReturn(successResponse);

            // When - Input: valid email and password
            ResultActions result = mockMvc.perform(post("/api/auth/token/candidate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)));

            // Then - Expected: 200 OK with access and refresh tokens
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.accessToken").exists())
                    .andExpect(jsonPath("$.result.refreshToken").exists())
                    .andExpect(jsonPath("$.result.authenticated").value(true));
        }

        @Test
        @DisplayName("TC011: Mobile login with banned user returns 403 Forbidden")
        void mobileLogin_WithBannedUser_Returns403() throws Exception {
            // Given - Precondition: User is banned
            when(authenticationImp.authenticateCandidate(any(AuthenticationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.ACCOUNT_BANNED));

            // When - Input: banned user credentials
            ResultActions result = mockMvc.perform(post("/api/auth/token/candidate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)));

            // Then - Expected: 403 Forbidden
            result.andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout - Logout")
    class LogoutTests {

        @Test
        @DisplayName("TC012: Logout with valid cookie returns 200 OK")
        void logout_WithValidCookie_Returns200() throws Exception {
            // Given - Precondition: User is logged in with valid refresh token
            Cookie refreshTokenCookie = new Cookie("refreshToken", "valid-refresh-token");

            // When - Input: valid refresh token cookie
            ResultActions result = mockMvc.perform(post("/api/auth/logout")
                    .cookie(refreshTokenCookie)
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK, cookie cleared
            result.andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC013: Logout without cookie still returns 200 OK")
        void logout_WithoutCookie_Returns200() throws Exception {
            // Given - Precondition: No cookie (already logged out or never logged in)

            // When - Input: no cookie
            ResultActions result = mockMvc.perform(post("/api/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON));

            // Then - Expected: 200 OK (graceful handling)
            result.andExpect(status().isOk());
        }
    }
}
