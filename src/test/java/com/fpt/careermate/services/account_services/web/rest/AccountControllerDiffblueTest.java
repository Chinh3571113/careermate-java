package com.fpt.careermate.services.account_services.web.rest;

import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fpt.careermate.common.exception.GlobalExceptionHandler;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.common.util.ChangePassword;
import com.fpt.careermate.services.account_services.service.AccountImp;
import com.fpt.careermate.services.account_services.service.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.account_services.service.dto.response.AccountResponse;
import com.fpt.careermate.services.account_services.service.dto.response.AccountResponse.AccountResponseBuilder;
import com.fpt.careermate.services.email_services.service.EmailImp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.FormLoginRequestBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {AccountController.class, GlobalExceptionHandler.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class AccountControllerDiffblueTest {
    @Autowired
    private AccountController accountController;

    @MockitoBean
    private AccountImp accountImp;

    @MockitoBean
    private EmailImp emailImp;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    /**
     * Test {@link AccountController#createUser(AccountCreationRequest)}.
     *
     * <p>Method under test: {@link AccountController#createUser(AccountCreationRequest)}
     */
    @Test
    @DisplayName("Test createUser(AccountCreationRequest)")
    @Tag("MaintainedByDiffblue")
    void testCreateUser() throws Exception {
        // Arrange
        AccountCreationRequest accountCreationRequest = new AccountCreationRequest();
        accountCreationRequest.setEmail("jane.doe@example.org");
        accountCreationRequest.setPassword(
                "com.fpt.careermate.services.account_services.service.dto.request.AccountCreationRequest");
        accountCreationRequest.setStatus("Status");
        accountCreationRequest.setUsername("janedoe");

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                JsonMapper.builder()
                                        .findAndAddModules()
                                        .build()
                                        .writeValueAsString(accountCreationRequest));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"message\":\"Validation failed\",\"errors\":{\"password\":\"Password must be at least 8 characters long and"
                                                + " contain at least one uppercase letter, one lowercase letter, one digit, and one special character\"}"
                                                + ",\"status\":400}"));
    }

    /**
     * Test {@link AccountController#createUser(AccountCreationRequest)}.
     *
     * <ul>
     *   <li>Given {@code .*[A-Z].*}.
     *   <li>When {@link AccountCreationRequest#AccountCreationRequest()} Password is {@code
     *       .*[A-Z].*}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#createUser(AccountCreationRequest)}
     */
    @Test
    @DisplayName(
            "Test createUser(AccountCreationRequest); given '.*[A-Z].*'; when AccountCreationRequest() Password is '.*[A-Z].*'")
    @Tag("MaintainedByDiffblue")
    void testCreateUser_givenAZ_whenAccountCreationRequestPasswordIsAZ() throws Exception {
        // Arrange
        AccountCreationRequest accountCreationRequest = new AccountCreationRequest();
        accountCreationRequest.setEmail("jane.doe@example.org");
        accountCreationRequest.setPassword(".*[A-Z].*");
        accountCreationRequest.setStatus("Status");
        accountCreationRequest.setUsername("janedoe");

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                JsonMapper.builder()
                                        .findAndAddModules()
                                        .build()
                                        .writeValueAsString(accountCreationRequest));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"message\":\"Validation failed\",\"errors\":{\"password\":\"Password must be at least 8 characters long and"
                                                + " contain at least one uppercase letter, one lowercase letter, one digit, and one special character\"}"
                                                + ",\"status\":400}"));
    }

    /**
     * Test {@link AccountController#createUser(AccountCreationRequest)}.
     *
     * <ul>
     *   <li>Given {@code iloveyou}.
     *   <li>When {@link AccountCreationRequest#AccountCreationRequest()} Password is {@code
     *       iloveyou}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#createUser(AccountCreationRequest)}
     */
    @Test
    @DisplayName(
            "Test createUser(AccountCreationRequest); given 'iloveyou'; when AccountCreationRequest() Password is 'iloveyou'")
    @Tag("MaintainedByDiffblue")
    void testCreateUser_givenIloveyou_whenAccountCreationRequestPasswordIsIloveyou()
            throws Exception {
        // Arrange
        AccountCreationRequest accountCreationRequest = new AccountCreationRequest();
        accountCreationRequest.setEmail("jane.doe@example.org");
        accountCreationRequest.setPassword("iloveyou");
        accountCreationRequest.setStatus("Status");
        accountCreationRequest.setUsername("janedoe");

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                JsonMapper.builder()
                                        .findAndAddModules()
                                        .build()
                                        .writeValueAsString(accountCreationRequest));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"message\":\"Validation failed\",\"errors\":{\"password\":\"Password must be at least 8 characters long and"
                                                + " contain at least one uppercase letter, one lowercase letter, one digit, and one special character\"}"
                                                + ",\"status\":400}"));
    }

    /**
     * Test {@link AccountController#createUser(AccountCreationRequest)}.
     *
     * <ul>
     *   <li>Given {@code Not blank!}.
     *   <li>When {@link AccountCreationRequest#AccountCreationRequest()} Password is {@code Not
     *       blank!}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#createUser(AccountCreationRequest)}
     */
    @Test
    @DisplayName(
            "Test createUser(AccountCreationRequest); given 'Not blank!'; when AccountCreationRequest() Password is 'Not blank!'")
    @Tag("MaintainedByDiffblue")
    void testCreateUser_givenNotBlank_whenAccountCreationRequestPasswordIsNotBlank()
            throws Exception {
        // Arrange
        AccountCreationRequest accountCreationRequest = new AccountCreationRequest();
        accountCreationRequest.setEmail("jane.doe@example.org");
        accountCreationRequest.setPassword("Not blank!");
        accountCreationRequest.setStatus("Status");
        accountCreationRequest.setUsername("janedoe");

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                JsonMapper.builder()
                                        .findAndAddModules()
                                        .build()
                                        .writeValueAsString(accountCreationRequest));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"message\":\"Validation failed\",\"errors\":{\"password\":\"Password must be at least 8 characters long and"
                                                + " contain at least one uppercase letter, one lowercase letter, one digit, and one special character\"}"
                                                + ",\"status\":400}"));
    }

    /**
     * Test {@link AccountController#createUser(AccountCreationRequest)}.
     *
     * <ul>
     *   <li>Given {@code Password}.
     *   <li>When {@link AccountCreationRequest#AccountCreationRequest()} Password is {@code
     *       Password}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#createUser(AccountCreationRequest)}
     */
    @Test
    @DisplayName(
            "Test createUser(AccountCreationRequest); given 'Password'; when AccountCreationRequest() Password is 'Password'")
    @Tag("MaintainedByDiffblue")
    void testCreateUser_givenPassword_whenAccountCreationRequestPasswordIsPassword()
            throws Exception {
        // Arrange
        AccountCreationRequest accountCreationRequest = new AccountCreationRequest();
        accountCreationRequest.setEmail("jane.doe@example.org");
        accountCreationRequest.setPassword("Password");
        accountCreationRequest.setStatus("Status");
        accountCreationRequest.setUsername("janedoe");

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                JsonMapper.builder()
                                        .findAndAddModules()
                                        .build()
                                        .writeValueAsString(accountCreationRequest));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"message\":\"Validation failed\",\"errors\":{\"password\":\"Password must be at least 8 characters long and"
                                                + " contain at least one uppercase letter, one lowercase letter, one digit, and one special character\"}"
                                                + ",\"status\":400}"));
    }

    /**
     * Test {@link AccountController#createUser(AccountCreationRequest)}.
     *
     * <ul>
     *   <li>Given {@code U-U-UUU.U-U-UUU.U-U-UUU}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#createUser(AccountCreationRequest)}
     */
    @Test
    @DisplayName("Test createUser(AccountCreationRequest); given 'U-U-UUU.U-U-UUU.U-U-UUU'")
    @Tag("MaintainedByDiffblue")
    void testCreateUser_givenUUUuuUUUuuUUUuu() throws Exception {
        // Arrange
        AccountCreationRequest accountCreationRequest = new AccountCreationRequest();
        accountCreationRequest.setEmail("jane.doe@example.org");
        accountCreationRequest.setPassword("U-U-UUU.U-U-UUU.U-U-UUU");
        accountCreationRequest.setStatus("Status");
        accountCreationRequest.setUsername("janedoe");

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                JsonMapper.builder()
                                        .findAndAddModules()
                                        .build()
                                        .writeValueAsString(accountCreationRequest));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"message\":\"Validation failed\",\"errors\":{\"password\":\"Password must be at least 8 characters long and"
                                                + " contain at least one uppercase letter, one lowercase letter, one digit, and one special character\"}"
                                                + ",\"status\":400}"));
    }

    /**
     * Test {@link AccountController#createUser(AccountCreationRequest)}.
     *
     * <ul>
     *   <li>Given {@code Validation failed}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#createUser(AccountCreationRequest)}
     */
    @Test
    @DisplayName("Test createUser(AccountCreationRequest); given 'Validation failed'")
    @Tag("MaintainedByDiffblue")
    void testCreateUser_givenValidationFailed() throws Exception {
        // Arrange
        AccountCreationRequest accountCreationRequest = new AccountCreationRequest();
        accountCreationRequest.setEmail("jane.doe@example.org");
        accountCreationRequest.setPassword("Validation failed");
        accountCreationRequest.setStatus("Status");
        accountCreationRequest.setUsername("janedoe");

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                JsonMapper.builder()
                                        .findAndAddModules()
                                        .build()
                                        .writeValueAsString(accountCreationRequest));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"message\":\"Validation failed\",\"errors\":{\"password\":\"Password must be at least 8 characters long and"
                                                + " contain at least one uppercase letter, one lowercase letter, one digit, and one special character\"}"
                                                + ",\"status\":400}"));
    }

    /**
     * Test {@link AccountController#getAllUsers(String, String, String, int, int)}.
     *
     * <ul>
     *   <li>When empty string.
     *   <li>Then status {@link StatusResultMatchers#isOk()}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#getAllUsers(String, String, String, int, int)}
     */
    @Test
    @DisplayName(
            "Test getAllUsers(String, String, String, int, int); when empty string; then status isOk()")
    @Tag("MaintainedByDiffblue")
    void testGetAllUsers_whenEmptyString_thenStatusIsOk() throws Exception {
        // Arrange
        PageResponse<AccountResponse> pageResponse = new PageResponse<>(new ArrayList<>(), 1, 3, 1L, 1);
        when(accountImp.searchAccounts(
                Mockito.<List<String>>any(),
                Mockito.<List<String>>any(),
                Mockito.<String>any(),
                Mockito.<Pageable>any()))
                .thenReturn(pageResponse);

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/api/users/all")
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(1))
                        .param("roles", "");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"code\":200,\"result\":{\"content\":[],\"page\":1,\"size\":3,\"totalElements\":1,\"totalPages\":1}}"));
    }

    /**
     * Test {@link AccountController#getAllUsers(String, String, String, int, int)}.
     *
     * <ul>
     *   <li>When {@code [,;]}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#getAllUsers(String, String, String, int, int)}
     */
    @Test
    @DisplayName("Test getAllUsers(String, String, String, int, int); when '[,;]'")
    @Tag("MaintainedByDiffblue")
    void testGetAllUsers_whenLeftSquareBracketCommaSemicolonRightSquareBracket() throws Exception {
        // Arrange
        PageResponse<AccountResponse> pageResponse = new PageResponse<>(new ArrayList<>(), 1, 3, 1L, 1);
        when(accountImp.searchAccounts(
                Mockito.<List<String>>any(),
                Mockito.<List<String>>any(),
                Mockito.<String>any(),
                Mockito.<Pageable>any()))
                .thenReturn(pageResponse);

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/api/users/all")
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(1))
                        .param("roles", "[,;]");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"code\":200,\"result\":{\"content\":[],\"page\":1,\"size\":3,\"totalElements\":1,\"totalPages\":1}}"));
    }

    /**
     * Test {@link AccountController#getAllUsers(String, String, String, int, int)}.
     *
     * <ul>
     *   <li>When {@link MockHttpServletRequestBuilder#param(String, String[])} {@code page} is
     *       valueOf one.
     *   <li>Then status {@link StatusResultMatchers#isOk()}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#getAllUsers(String, String, String, int, int)}
     */
    @Test
    @DisplayName(
            "Test getAllUsers(String, String, String, int, int); when param(String, String[]) 'page' is valueOf one; then status isOk()")
    @Tag("MaintainedByDiffblue")
    void testGetAllUsers_whenParamPageIsValueOfOne_thenStatusIsOk() throws Exception {
        // Arrange
        PageResponse<AccountResponse> pageResponse = new PageResponse<>(new ArrayList<>(), 1, 3, 1L, 1);
        when(accountImp.searchAccounts(
                Mockito.<List<String>>any(),
                Mockito.<List<String>>any(),
                Mockito.<String>any(),
                Mockito.<Pageable>any()))
                .thenReturn(pageResponse);

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/api/users/all")
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(1));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"code\":200,\"result\":{\"content\":[],\"page\":1,\"size\":3,\"totalElements\":1,\"totalPages\":1}}"));
    }

    /**
     * Test {@link AccountController#getAllUsers(String, String, String, int, int)}.
     *
     * <ul>
     *   <li>When {@link MockHttpServletRequestBuilder#param(String, String[])} {@code roles} is
     *       {@code foo}.
     *   <li>Then status {@link StatusResultMatchers#isOk()}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#getAllUsers(String, String, String, int, int)}
     */
    @Test
    @DisplayName(
            "Test getAllUsers(String, String, String, int, int); when param(String, String[]) 'roles' is 'foo'; then status isOk()")
    @Tag("MaintainedByDiffblue")
    void testGetAllUsers_whenParamRolesIsFoo_thenStatusIsOk() throws Exception {
        // Arrange
        PageResponse<AccountResponse> pageResponse = new PageResponse<>(new ArrayList<>(), 1, 3, 1L, 1);
        when(accountImp.searchAccounts(
                Mockito.<List<String>>any(),
                Mockito.<List<String>>any(),
                Mockito.<String>any(),
                Mockito.<Pageable>any()))
                .thenReturn(pageResponse);

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/api/users/all")
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(1))
                        .param("roles", "foo");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"code\":200,\"result\":{\"content\":[],\"page\":1,\"size\":3,\"totalElements\":1,\"totalPages\":1}}"));
    }

    /**
     * Test {@link AccountController#getAllUsers(String, String, String, int, int)}.
     *
     * <ul>
     *   <li>When {@link MockHttpServletRequestBuilder#param(String, String[])} {@code statuses} is
     *       {@code foo}.
     *   <li>Then status {@link StatusResultMatchers#isOk()}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#getAllUsers(String, String, String, int, int)}
     */
    @Test
    @DisplayName(
            "Test getAllUsers(String, String, String, int, int); when param(String, String[]) 'statuses' is 'foo'; then status isOk()")
    @Tag("MaintainedByDiffblue")
    void testGetAllUsers_whenParamStatusesIsFoo_thenStatusIsOk() throws Exception {
        // Arrange
        PageResponse<AccountResponse> pageResponse = new PageResponse<>(new ArrayList<>(), 1, 3, 1L, 1);
        when(accountImp.searchAccounts(
                Mockito.<List<String>>any(),
                Mockito.<List<String>>any(),
                Mockito.<String>any(),
                Mockito.<Pageable>any()))
                .thenReturn(pageResponse);

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/api/users/all")
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(1))
                        .param("statuses", "foo");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"code\":200,\"result\":{\"content\":[],\"page\":1,\"size\":3,\"totalElements\":1,\"totalPages\":1}}"));
    }

    /**
     * Test {@link AccountController#getAllUsers(String, String, String, int, int)}.
     *
     * <ul>
     *   <li>When valueOf minus one.
     *   <li>Then status four hundred.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#getAllUsers(String, String, String, int, int)}
     */
    @Test
    @DisplayName(
            "Test getAllUsers(String, String, String, int, int); when valueOf minus one; then status four hundred")
    @Tag("MaintainedByDiffblue")
    void testGetAllUsers_whenValueOfMinusOne_thenStatusFourHundred() throws Exception {
        // Arrange
        PageResponse<AccountResponse> pageResponse = new PageResponse<>(new ArrayList<>(), 1, 3, 1L, 1);
        when(accountImp.searchAccounts(
                Mockito.<List<String>>any(),
                Mockito.<List<String>>any(),
                Mockito.<String>any(),
                Mockito.<Pageable>any()))
                .thenReturn(pageResponse);

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/api/users/all")
                        .param("page", String.valueOf(-1))
                        .param("size", String.valueOf(1));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("{\"code\":9999,\"message\":\"Uncategorized error\"}"));
    }

    /**
     * Test {@link AccountController#getAllUsers(String, String, String, int, int)}.
     *
     * <ul>
     *   <li>When {@code Values}.
     *   <li>Then status four hundred.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#getAllUsers(String, String, String, int, int)}
     */
    @Test
    @DisplayName(
            "Test getAllUsers(String, String, String, int, int); when 'Values'; then status four hundred")
    @Tag("MaintainedByDiffblue")
    void testGetAllUsers_whenValues_thenStatusFourHundred() throws Exception {
        // Arrange
        PageResponse<AccountResponse> pageResponse = new PageResponse<>(new ArrayList<>(), 1, 3, 1L, 1);
        when(accountImp.searchAccounts(
                Mockito.<List<String>>any(),
                Mockito.<List<String>>any(),
                Mockito.<String>any(),
                Mockito.<Pageable>any()))
                .thenReturn(pageResponse);

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/api/users/all")
                        .param("page", "Values")
                        .param("size", String.valueOf(1));

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("{\"code\":9999,\"message\":\"Uncategorized error\"}"));
    }

    /**
     * Test {@link AccountController#getUserById(int)}.
     *
     * <ul>
     *   <li>Given array of {@link Object} with one.
     *   <li>When {@code Id}.
     *   <li>Then status four hundred.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#getUserById(int)}
     */
    @Test
    @DisplayName(
            "Test getUserById(int); given array of Object with one; when 'Id'; then status four hundred")
    @Tag("MaintainedByDiffblue")
    void testGetUserById_givenArrayOfObjectWithOne_whenId_thenStatusFourHundred() throws Exception {
        // Arrange
        AccountResponseBuilder idResult = AccountResponse.builder().email("jane.doe@example.org").id(1);
        when(accountImp.getAccountById(anyInt()))
                .thenReturn(idResult.roles(new HashSet<>()).status("Status").username("janedoe").build());

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/api/users/{id}", "Id");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("{\"code\":9999,\"message\":\"Uncategorized error\"}"));
    }

    /**
     * Test {@link AccountController#getUserById(int)}.
     *
     * <ul>
     *   <li>When one.
     *   <li>Then status {@link StatusResultMatchers#isOk()}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#getUserById(int)}
     */
    @Test
    @DisplayName("Test getUserById(int); when one; then status isOk()")
    @Tag("MaintainedByDiffblue")
    void testGetUserById_whenOne_thenStatusIsOk() throws Exception {
        // Arrange
        AccountResponseBuilder idResult = AccountResponse.builder().email("jane.doe@example.org").id(1);
        when(accountImp.getAccountById(anyInt()))
                .thenReturn(idResult.roles(new HashSet<>()).status("Status").username("janedoe").build());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/users/{id}", 1);

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"code\":200,\"result\":{\"id\":1,\"username\":\"janedoe\",\"email\":\"jane.doe@example.org\",\"status\":\"Status\","
                                                + "\"roles\":[]}}"));
    }

    /**
     * Test {@link AccountController#getCurrentUser()}.
     *
     * <p>Method under test: {@link AccountController#getCurrentUser()}
     */
    @Test
    @DisplayName("Test getCurrentUser()")
    @Tag("MaintainedByDiffblue")
    void testGetCurrentUser() throws Exception {
        // Arrange
        AccountResponseBuilder idResult = AccountResponse.builder().email("jane.doe@example.org").id(1);
        when(accountImp.getCurrentUser())
                .thenReturn(idResult.roles(new HashSet<>()).status("Status").username("janedoe").build());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/users/current");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"code\":200,\"result\":{\"id\":1,\"username\":\"janedoe\",\"email\":\"jane.doe@example.org\",\"status\":\"Status\","
                                                + "\"roles\":[]}}"));
    }

    /**
     * Test {@link AccountController#deleteUser(int)}.
     *
     * <p>Method under test: {@link AccountController#deleteUser(int)}
     */
    @Test
    @DisplayName("Test deleteUser(int)")
    @Tag("MaintainedByDiffblue")
    void testDeleteUser() throws Exception {
        // Arrange
        doNothing().when(accountImp).deleteAccount(anyInt());

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.delete(
                        "/api/users/{id}",
                        "com.fpt.careermate.services.account_services.web.rest.AccountController");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("{\"code\":9999,\"message\":\"Uncategorized error\"}"));
    }

    /**
     * Test {@link AccountController#deleteUser(int)}.
     *
     * <ul>
     *   <li>Given {@code /api/users/{id}}.
     *   <li>When formLogin.
     *   <li>Then status {@link StatusResultMatchers#isNotFound()}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#deleteUser(int)}
     */
    @Test
    @DisplayName(
            "Test deleteUser(int); given '/api/users/{id}'; when formLogin; then status isNotFound()")
    @Tag("MaintainedByDiffblue")
    void testDeleteUser_givenApiUsersId_whenFormLogin_thenStatusIsNotFound() throws Exception {
        // Arrange
        doNothing().when(accountImp).deleteAccount(anyInt());

        FormLoginRequestBuilder requestBuilder = SecurityMockMvcRequestBuilders.formLogin();

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isNotFound());
    }

    /**
     * Test {@link AccountController#deleteUser(int)}.
     *
     * <ul>
     *   <li>Given array of {@link Object} with one.
     *   <li>When {@code Id}.
     *   <li>Then status four hundred.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#deleteUser(int)}
     */
    @Test
    @DisplayName(
            "Test deleteUser(int); given array of Object with one; when 'Id'; then status four hundred")
    @Tag("MaintainedByDiffblue")
    void testDeleteUser_givenArrayOfObjectWithOne_whenId_thenStatusFourHundred() throws Exception {
        // Arrange
        doNothing().when(accountImp).deleteAccount(anyInt());

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.delete("/api/users/{id}", "Id");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("{\"code\":9999,\"message\":\"Uncategorized error\"}"));
    }

    /**
     * Test {@link AccountController#deleteUser(int)}.
     *
     * <ul>
     *   <li>When one.
     *   <li>Then status {@link StatusResultMatchers#isOk()}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#deleteUser(int)}
     */
    @Test
    @DisplayName("Test deleteUser(int); when one; then status isOk()")
    @Tag("MaintainedByDiffblue")
    void testDeleteUser_whenOne_thenStatusIsOk() throws Exception {
        // Arrange
        doNothing().when(accountImp).deleteAccount(anyInt());

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.delete("/api/users/{id}", 1);

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("{\"code\":200,\"message\":\"Delete account successfully\"}"));
    }

    /**
     * Test {@link AccountController#forgetPassword(String)}.
     *
     * <p>Method under test: {@link AccountController#forgetPassword(String)}
     */
    @Test
    @DisplayName("Test forgetPassword(String)")
    @Tag("MaintainedByDiffblue")
    void testForgetPassword() throws Exception {
        // Arrange
        when(emailImp.verifyEmail(Mockito.<String>any())).thenReturn("jane.doe@example.org");

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/api/users/verify-email/{email}", "jane.doe@example.org");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"code\":200,\"message\":\"If the email exists, a password reset link has been sent.\",\"result\":\"jane.doe"
                                                + "@example.org\"}"));
    }

    /**
     * Test {@link AccountController#verifyCode(String, Integer)}.
     *
     * <ul>
     *   <li>When {@code Code verified successfully}.
     *   <li>Then status four hundred.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#verifyCode(String, Integer)}
     */
    @Test
    @DisplayName(
            "Test verifyCode(String, Integer); when 'Code verified successfully'; then status four hundred")
    @Tag("MaintainedByDiffblue")
    void testVerifyCode_whenCodeVerifiedSuccessfully_thenStatusFourHundred() throws Exception {
        // Arrange
        when(emailImp.verifyOtp(Mockito.<String>any(), Mockito.<Integer>any()))
                .thenReturn("Verify Otp");

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/api/users/verify-otp")
                        .param("code", "Code verified successfully")
                        .param("email", "foo");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().is(400))
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("{\"code\":9999,\"message\":\"Uncategorized error\"}"));
    }

    /**
     * Test {@link AccountController#verifyCode(String, Integer)}.
     *
     * <ul>
     *   <li>When valueOf one.
     *   <li>Then status {@link StatusResultMatchers#isOk()}.
     * </ul>
     *
     * <p>Method under test: {@link AccountController#verifyCode(String, Integer)}
     */
    @Test
    @DisplayName("Test verifyCode(String, Integer); when valueOf one; then status isOk()")
    @Tag("MaintainedByDiffblue")
    void testVerifyCode_whenValueOfOne_thenStatusIsOk() throws Exception {
        // Arrange
        when(emailImp.verifyOtp(Mockito.<String>any(), Mockito.<Integer>any()))
                .thenReturn("Verify Otp");

        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/api/users/verify-otp")
                        .param("code", String.valueOf(1))
                        .param("email", "foo");

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"code\":200,\"message\":\"Code verified successfully\",\"result\":\"Verify Otp\"}"));
    }

    /**
     * Test {@link AccountController#changePassword(ChangePassword, String)}.
     *
     * <p>Method under test: {@link AccountController#changePassword(ChangePassword, String)}
     */
    @Test
    @DisplayName("Test changePassword(ChangePassword, String)")
    @Tag("MaintainedByDiffblue")
    void testChangePassword() throws Exception {
        // Arrange
        when(emailImp.changePassword(Mockito.<ChangePassword>any(), Mockito.<String>any()))
                .thenReturn("iloveyou");

        MockHttpServletRequestBuilder contentTypeResult =
                MockMvcRequestBuilders.put("/api/users/change-password/{email}", "jane.doe@example.org")
                        .contentType(MediaType.APPLICATION_JSON);

        JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();
        String content = jsonMapper.writeValueAsString(new ChangePassword("iloveyou", "iloveyou"));

        MockHttpServletRequestBuilder requestBuilder = contentTypeResult.content(content);

        // Act and Assert
        MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(globalExceptionHandler)
                .build()
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(
                        content()
                                .string(
                                        "{\"code\":200,\"message\":\"Password changed successfully\",\"result\":\"iloveyou\"}"));
    }
}
