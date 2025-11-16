package com.fpt.careermate.services.account_services.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.account_services.service.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.account_services.service.dto.response.AccountResponse;
import com.fpt.careermate.services.account_services.service.dto.response.AccountResponse.AccountResponseBuilder;
import com.fpt.careermate.services.account_services.service.mapper.AccountMapper;
import com.fpt.careermate.services.account_services.service.mapper.AccountMapperImpl;
import com.fpt.careermate.services.authentication_services.domain.ForgotPassword;
import com.fpt.careermate.services.authentication_services.domain.Role;
import com.fpt.careermate.services.authentication_services.repository.InvalidDateTokenRepo;
import com.fpt.careermate.services.authentication_services.repository.RoleRepo;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {AccountImp.class, PasswordEncoder.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class AccountImpDiffblueTest {
    @InjectMocks
    private AccountImp accountImp;

    @Autowired
    private AccountImp accountImp2;

    @Mock
    private AccountMapper accountMapper;

    @MockitoBean
    private AccountMapper accountMapper2;

    @Mock
    private AccountRepo accountRepo;

    @MockitoBean
    private AccountRepo accountRepo2;

    @MockitoBean
    private AuthenticationImp authenticationImp;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RoleRepo roleRepo;

    /**
     * Test {@link AccountImp#createAccount(AccountCreationRequest)}.
     *
     * <p>Method under test: {@link AccountImp#createAccount(AccountCreationRequest)}
     */
    @Test
    @DisplayName("Test createAccount(AccountCreationRequest)")
    @Tag("MaintainedByDiffblue")
    void testCreateAccount() {
        // Arrange
        when(accountRepo2.existsByEmail(Mockito.<String>any())).thenReturn(false);
        when(roleRepo.findById(Mockito.<String>any()))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        Account account = new Account();
        account.setEmail("jane.doe@example.org");
        account.setForgotPassword(new ForgotPassword());
        account.setId(1);
        account.setPassword("iloveyou");
        account.setRoles(new HashSet<>());
        account.setStatus("Status");
        account.setUsername("janedoe");

        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setAccount(account);
        forgotPassword.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword.setFpid(1);
        forgotPassword.setOtp(1);

        Account account2 = new Account();
        account2.setEmail("jane.doe@example.org");
        account2.setForgotPassword(forgotPassword);
        account2.setId(1);
        account2.setPassword("iloveyou");
        account2.setRoles(new HashSet<>());
        account2.setStatus("Status");
        account2.setUsername("janedoe");

        ForgotPassword forgotPassword2 = new ForgotPassword();
        forgotPassword2.setAccount(account2);
        forgotPassword2.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword2.setFpid(1);
        forgotPassword2.setOtp(1);

        Account account3 = new Account();
        account3.setEmail("jane.doe@example.org");
        account3.setForgotPassword(forgotPassword2);
        account3.setId(1);
        account3.setPassword("iloveyou");
        account3.setRoles(new HashSet<>());
        account3.setStatus("Status");
        account3.setUsername("janedoe");
        when(accountMapper2.toAccount(Mockito.<AccountCreationRequest>any())).thenReturn(account3);
        AccountCreationRequest request =
                new AccountCreationRequest("janedoe", "jane.doe@example.org", "iloveyou", "Status");

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp2.createAccount(request));
        verify(accountRepo2).existsByEmail("jane.doe@example.org");
        verify(accountMapper2).toAccount(isA(AccountCreationRequest.class));
        verify(roleRepo).findById("CANDIDATE");
    }

    /**
     * Test {@link AccountImp#createAccount(AccountCreationRequest)}.
     *
     * <p>Method under test: {@link AccountImp#createAccount(AccountCreationRequest)}
     */
    @Test
    @DisplayName("Test createAccount(AccountCreationRequest)")
    @Tag("MaintainedByDiffblue")
    void testCreateAccount2() {
        // Arrange
        when(accountRepo2.existsByEmail(Mockito.<String>any()))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        AccountCreationRequest request =
                new AccountCreationRequest("janedoe", "jane.doe@example.org", "iloveyou", "Status");

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp2.createAccount(request));
        verify(accountRepo2).existsByEmail("jane.doe@example.org");
    }

    /**
     * Test {@link AccountImp#createAccount(AccountCreationRequest)}.
     *
     * <p>Method under test: {@link AccountImp#createAccount(AccountCreationRequest)}
     */
    @Test
    @DisplayName("Test createAccount(AccountCreationRequest)")
    @Tag("MaintainedByDiffblue")
    void testCreateAccount3() {
        // Arrange
        Account account = new Account();
        account.setEmail("jane.doe@example.org");
        account.setForgotPassword(new ForgotPassword());
        account.setId(1);
        account.setPassword("iloveyou");
        account.setRoles(new HashSet<>());
        account.setStatus("Status");
        account.setUsername("janedoe");

        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setAccount(account);
        forgotPassword.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword.setFpid(1);
        forgotPassword.setOtp(1);

        Account account2 = new Account();
        account2.setEmail("jane.doe@example.org");
        account2.setForgotPassword(forgotPassword);
        account2.setId(1);
        account2.setPassword("iloveyou");
        account2.setRoles(new HashSet<>());
        account2.setStatus("Status");
        account2.setUsername("janedoe");

        ForgotPassword forgotPassword2 = new ForgotPassword();
        forgotPassword2.setAccount(account2);
        forgotPassword2.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword2.setFpid(1);
        forgotPassword2.setOtp(1);

        Account account3 = new Account();
        account3.setEmail("jane.doe@example.org");
        account3.setForgotPassword(forgotPassword2);
        account3.setId(1);
        account3.setPassword("iloveyou");
        account3.setRoles(new HashSet<>());
        account3.setStatus("Status");
        account3.setUsername("janedoe");
        when(accountRepo2.save(Mockito.<Account>any())).thenReturn(account3);
        when(accountRepo2.existsByEmail(Mockito.<String>any())).thenReturn(false);

        Role role = new Role();
        role.setDescription("The characteristics of someone or something");
        role.setName("Name");
        role.setPermissions(new HashSet<>());
        Optional<Role> ofResult = Optional.of(role);
        when(roleRepo.findById(Mockito.<String>any())).thenReturn(ofResult);

        Account account4 = new Account();
        account4.setEmail("jane.doe@example.org");
        account4.setForgotPassword(new ForgotPassword());
        account4.setId(1);
        account4.setPassword("iloveyou");
        account4.setRoles(new HashSet<>());
        account4.setStatus("Status");
        account4.setUsername("janedoe");

        ForgotPassword forgotPassword3 = new ForgotPassword();
        forgotPassword3.setAccount(account4);
        forgotPassword3.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword3.setFpid(1);
        forgotPassword3.setOtp(1);

        Account account5 = new Account();
        account5.setEmail("jane.doe@example.org");
        account5.setForgotPassword(forgotPassword3);
        account5.setId(1);
        account5.setPassword("iloveyou");
        account5.setRoles(new HashSet<>());
        account5.setStatus("Status");
        account5.setUsername("janedoe");

        ForgotPassword forgotPassword4 = new ForgotPassword();
        forgotPassword4.setAccount(account5);
        forgotPassword4.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword4.setFpid(1);
        forgotPassword4.setOtp(1);

        Account account6 = new Account();
        account6.setEmail("jane.doe@example.org");
        account6.setForgotPassword(forgotPassword4);
        account6.setId(1);
        account6.setPassword("iloveyou");
        account6.setRoles(new HashSet<>());
        account6.setStatus("Status");
        account6.setUsername("janedoe");
        when(accountMapper2.toAccountResponse(Mockito.<Account>any()))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        when(accountMapper2.toAccount(Mockito.<AccountCreationRequest>any())).thenReturn(account6);
        AccountCreationRequest request =
                new AccountCreationRequest("janedoe", "jane.doe@example.org", "iloveyou", "Status");

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp2.createAccount(request));
        verify(accountRepo2).existsByEmail("jane.doe@example.org");
        verify(accountMapper2).toAccount(isA(AccountCreationRequest.class));
        verify(accountMapper2).toAccountResponse(isA(Account.class));
        verify(roleRepo).findById("CANDIDATE");
        verify(accountRepo2).save(isA(Account.class));
    }

    /**
     * Test {@link AccountImp#createAccount(AccountCreationRequest)}.
     *
     * <p>Method under test: {@link AccountImp#createAccount(AccountCreationRequest)}
     */
    @Test
    @DisplayName("Test createAccount(AccountCreationRequest)")
    @Tag("MaintainedByDiffblue")
    void testCreateAccount4() {
        // Arrange
        when(accountRepo2.existsByEmail(Mockito.<String>any())).thenReturn(false);
        when(accountMapper2.toAccount(Mockito.<AccountCreationRequest>any()))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        AccountCreationRequest request =
                new AccountCreationRequest("janedoe", "jane.doe@example.org", "iloveyou", "Status");

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp2.createAccount(request));
        verify(accountRepo2).existsByEmail("jane.doe@example.org");
        verify(accountMapper2).toAccount(isA(AccountCreationRequest.class));
    }

    /**
     * Test {@link AccountImp#createAccount(AccountCreationRequest)}.
     *
     * <ul>
     *   <li>Given {@link AccountRepo} {@link AccountRepo#existsByEmail(String)} return {@code true}.
     *   <li>Then throw {@link AppException}.
     * </ul>
     *
     * <p>Method under test: {@link AccountImp#createAccount(AccountCreationRequest)}
     */
    @Test
    @DisplayName(
            "Test createAccount(AccountCreationRequest); given AccountRepo existsByEmail(String) return 'true'; then throw AppException")
    @Tag("MaintainedByDiffblue")
    void testCreateAccount_givenAccountRepoExistsByEmailReturnTrue_thenThrowAppException() {
        // Arrange
        when(accountRepo2.existsByEmail(Mockito.<String>any())).thenReturn(true);
        AccountCreationRequest request =
                new AccountCreationRequest("janedoe", "jane.doe@example.org", "iloveyou", "Status");

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp2.createAccount(request));
        verify(accountRepo2).existsByEmail("jane.doe@example.org");
    }

    /**
     * Test {@link AccountImp#createAccount(AccountCreationRequest)}.
     *
     * <ul>
     *   <li>Then return {@code Status}.
     * </ul>
     *
     * <p>Method under test: {@link AccountImp#createAccount(AccountCreationRequest)}
     */
    @Test
    @DisplayName("Test createAccount(AccountCreationRequest); then return 'Status'")
    @Tag("MaintainedByDiffblue")
    void testCreateAccount_thenReturnStatus() {
        // Arrange
        Account account = new Account();
        account.setEmail("jane.doe@example.org");
        account.setForgotPassword(new ForgotPassword());
        account.setId(1);
        account.setPassword("iloveyou");
        account.setRoles(new HashSet<>());
        account.setStatus("Status");
        account.setUsername("janedoe");

        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setAccount(account);
        forgotPassword.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword.setFpid(1);
        forgotPassword.setOtp(1);

        Account account2 = new Account();
        account2.setEmail("jane.doe@example.org");
        account2.setForgotPassword(forgotPassword);
        account2.setId(1);
        account2.setPassword("iloveyou");
        account2.setRoles(new HashSet<>());
        account2.setStatus("Status");
        account2.setUsername("janedoe");

        ForgotPassword forgotPassword2 = new ForgotPassword();
        forgotPassword2.setAccount(account2);
        forgotPassword2.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword2.setFpid(1);
        forgotPassword2.setOtp(1);

        Account account3 = new Account();
        account3.setEmail("jane.doe@example.org");
        account3.setForgotPassword(forgotPassword2);
        account3.setId(1);
        account3.setPassword("iloveyou");
        account3.setRoles(new HashSet<>());
        account3.setStatus("Status");
        account3.setUsername("janedoe");
        when(accountRepo2.save(Mockito.<Account>any())).thenReturn(account3);
        when(accountRepo2.existsByEmail(Mockito.<String>any())).thenReturn(false);

        Role role = new Role();
        role.setDescription("The characteristics of someone or something");
        role.setName("Name");
        role.setPermissions(new HashSet<>());
        Optional<Role> ofResult = Optional.of(role);
        when(roleRepo.findById(Mockito.<String>any())).thenReturn(ofResult);

        Account account4 = new Account();
        account4.setEmail("jane.doe@example.org");
        account4.setForgotPassword(new ForgotPassword());
        account4.setId(1);
        account4.setPassword("iloveyou");
        account4.setRoles(new HashSet<>());
        account4.setStatus("Status");
        account4.setUsername("janedoe");

        ForgotPassword forgotPassword3 = new ForgotPassword();
        forgotPassword3.setAccount(account4);
        forgotPassword3.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword3.setFpid(1);
        forgotPassword3.setOtp(1);

        Account account5 = new Account();
        account5.setEmail("jane.doe@example.org");
        account5.setForgotPassword(forgotPassword3);
        account5.setId(1);
        account5.setPassword("iloveyou");
        account5.setRoles(new HashSet<>());
        account5.setStatus("Status");
        account5.setUsername("janedoe");

        ForgotPassword forgotPassword4 = new ForgotPassword();
        forgotPassword4.setAccount(account5);
        forgotPassword4.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword4.setFpid(1);
        forgotPassword4.setOtp(1);

        Account account6 = new Account();
        account6.setEmail("jane.doe@example.org");
        account6.setForgotPassword(forgotPassword4);
        account6.setId(1);
        account6.setPassword("iloveyou");
        account6.setRoles(new HashSet<>());
        account6.setStatus("Status");
        account6.setUsername("janedoe");

        AccountResponseBuilder idResult = AccountResponse.builder().email("jane.doe@example.org").id(1);
        when(accountMapper2.toAccountResponse(Mockito.<Account>any()))
                .thenReturn(idResult.roles(new HashSet<>()).status("Status").username("janedoe").build());
        when(accountMapper2.toAccount(Mockito.<AccountCreationRequest>any())).thenReturn(account6);
        AccountCreationRequest request =
                new AccountCreationRequest("janedoe", "jane.doe@example.org", "iloveyou", "Status");

        // Act
        AccountResponse actualCreateAccountResult = accountImp2.createAccount(request);

        // Assert
        verify(accountRepo2).existsByEmail("jane.doe@example.org");
        verify(accountMapper2).toAccount(isA(AccountCreationRequest.class));
        verify(accountMapper2).toAccountResponse(isA(Account.class));
        verify(roleRepo).findById("CANDIDATE");
        verify(accountRepo2).save(isA(Account.class));
        assertEquals("Status", actualCreateAccountResult.getStatus());
        assertEquals("jane.doe@example.org", actualCreateAccountResult.getEmail());
        assertEquals("janedoe", actualCreateAccountResult.getUsername());
        assertEquals(1, actualCreateAccountResult.getId());
        assertTrue(actualCreateAccountResult.getRoles().isEmpty());
    }

    /**
     * Test {@link AccountImp#getAccountById(int)}.
     *
     * <p>Method under test: {@link AccountImp#getAccountById(int)}
     */
    @Test
    @DisplayName("Test getAccountById(int)")
    @Tag("MaintainedByDiffblue")
    void testGetAccountById() {
        // Arrange
        AccountRepo accountRepo = mock(AccountRepo.class);
        when(accountRepo.findById(Mockito.<Integer>any()))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        RoleRepo roleRepo = mock(RoleRepo.class);
        AccountMapperImpl accountMapper = new AccountMapperImpl();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationImp authenticationImp =
                new AuthenticationImp(mock(InvalidDateTokenRepo.class), mock(AccountRepo.class));

        AccountImp accountImp =
                new AccountImp(accountRepo, roleRepo, accountMapper, passwordEncoder, authenticationImp);

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp.getAccountById(1));
        verify(accountRepo).findById(1);
    }

    /**
     * Test {@link AccountImp#getAccountById(int)}.
     *
     * <ul>
     *   <li>Given {@link AccountRepo} {@link AccountRepo#findById(Object)} return empty.
     *   <li>Then throw {@link AppException}.
     * </ul>
     *
     * <p>Method under test: {@link AccountImp#getAccountById(int)}
     */
    @Test
    @DisplayName(
            "Test getAccountById(int); given AccountRepo findById(Object) return empty; then throw AppException")
    @Tag("MaintainedByDiffblue")
    void testGetAccountById_givenAccountRepoFindByIdReturnEmpty_thenThrowAppException() {
        // Arrange
        AccountRepo accountRepo = mock(AccountRepo.class);
        Optional<Account> emptyResult = Optional.empty();
        when(accountRepo.findById(Mockito.<Integer>any())).thenReturn(emptyResult);
        RoleRepo roleRepo = mock(RoleRepo.class);
        AccountMapperImpl accountMapper = new AccountMapperImpl();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationImp authenticationImp =
                new AuthenticationImp(mock(InvalidDateTokenRepo.class), mock(AccountRepo.class));

        AccountImp accountImp =
                new AccountImp(accountRepo, roleRepo, accountMapper, passwordEncoder, authenticationImp);

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp.getAccountById(1));
        verify(accountRepo).findById(1);
    }

    /**
     * Test {@link AccountImp#getAccountById(int)}.
     *
     * <ul>
     *   <li>Given {@link ForgotPassword#ForgotPassword()} Account is {@link Account#Account()}.
     *   <li>Then return {@code Status}.
     * </ul>
     *
     * <p>Method under test: {@link AccountImp#getAccountById(int)}
     */
    @Test
    @DisplayName(
            "Test getAccountById(int); given ForgotPassword() Account is Account(); then return 'Status'")
    @Tag("MaintainedByDiffblue")
    void testGetAccountById_givenForgotPasswordAccountIsAccount_thenReturnStatus() {
        // Arrange
        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setAccount(new Account());
        forgotPassword.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword.setFpid(1);
        forgotPassword.setOtp(1);

        Account account = new Account();
        account.setEmail("jane.doe@example.org");
        account.setForgotPassword(forgotPassword);
        account.setId(1);
        account.setPassword("iloveyou");
        account.setRoles(new HashSet<>());
        account.setStatus("Status");
        account.setUsername("janedoe");

        ForgotPassword forgotPassword2 = new ForgotPassword();
        forgotPassword2.setAccount(account);
        forgotPassword2.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword2.setFpid(1);
        forgotPassword2.setOtp(1);

        Account account2 = new Account();
        account2.setEmail("jane.doe@example.org");
        account2.setForgotPassword(forgotPassword2);
        account2.setId(1);
        account2.setPassword("iloveyou");
        account2.setRoles(new HashSet<>());
        account2.setStatus("Status");
        account2.setUsername("janedoe");
        Optional<Account> ofResult = Optional.of(account2);

        AccountRepo accountRepo = mock(AccountRepo.class);
        when(accountRepo.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        RoleRepo roleRepo = mock(RoleRepo.class);
        AccountMapperImpl accountMapper = new AccountMapperImpl();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationImp authenticationImp =
                new AuthenticationImp(mock(InvalidDateTokenRepo.class), mock(AccountRepo.class));

        AccountImp accountImp =
                new AccountImp(accountRepo, roleRepo, accountMapper, passwordEncoder, authenticationImp);

        // Act
        AccountResponse actualAccountById = accountImp.getAccountById(1);

        // Assert
        verify(accountRepo).findById(1);
        assertEquals("Status", actualAccountById.getStatus());
        assertEquals("jane.doe@example.org", actualAccountById.getEmail());
        assertEquals("janedoe", actualAccountById.getUsername());
        assertEquals(1, actualAccountById.getId());
        assertTrue(actualAccountById.getRoles().isEmpty());
    }

    /**
     * Test {@link AccountImp#getAccountById(int)}.
     *
     * <ul>
     *   <li>Then calls {@link AccountMapper#toAccountResponse(Account)}.
     * </ul>
     *
     * <p>Method under test: {@link AccountImp#getAccountById(int)}
     */
    @Test
    @DisplayName("Test getAccountById(int); then calls toAccountResponse(Account)")
    @Tag("MaintainedByDiffblue")
    void testGetAccountById_thenCallsToAccountResponse() {
        // Arrange
        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setAccount(new Account());
        forgotPassword.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword.setFpid(1);
        forgotPassword.setOtp(1);

        Account account = new Account();
        account.setEmail("jane.doe@example.org");
        account.setForgotPassword(forgotPassword);
        account.setId(1);
        account.setPassword("iloveyou");
        account.setRoles(new HashSet<>());
        account.setStatus("Status");
        account.setUsername("janedoe");

        ForgotPassword forgotPassword2 = new ForgotPassword();
        forgotPassword2.setAccount(account);
        forgotPassword2.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword2.setFpid(1);
        forgotPassword2.setOtp(1);

        Account account2 = new Account();
        account2.setEmail("jane.doe@example.org");
        account2.setForgotPassword(forgotPassword2);
        account2.setId(1);
        account2.setPassword("iloveyou");
        account2.setRoles(new HashSet<>());
        account2.setStatus("Status");
        account2.setUsername("janedoe");
        Optional<Account> ofResult = Optional.of(account2);
        when(accountRepo.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        when(accountMapper.toAccountResponse(Mockito.<Account>any()))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp.getAccountById(1));
        verify(accountMapper).toAccountResponse(isA(Account.class));
        verify(accountRepo).findById(1);
    }

    /**
     * Test {@link AccountImp#deleteAccount(int)}.
     *
     * <p>Method under test: {@link AccountImp#deleteAccount(int)}
     */
    @Test
    @DisplayName("Test deleteAccount(int)")
    @Tag("MaintainedByDiffblue")
    void testDeleteAccount() {
        // Arrange
        AccountRepo accountRepo = mock(AccountRepo.class);
        when(accountRepo.findById(Mockito.<Integer>any()))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        RoleRepo roleRepo = mock(RoleRepo.class);
        AccountMapperImpl accountMapper = new AccountMapperImpl();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationImp authenticationImp =
                new AuthenticationImp(mock(InvalidDateTokenRepo.class), mock(AccountRepo.class));

        AccountImp accountImp =
                new AccountImp(accountRepo, roleRepo, accountMapper, passwordEncoder, authenticationImp);

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp.deleteAccount(1));
        verify(accountRepo).findById(1);
    }

    /**
     * Test {@link AccountImp#deleteAccount(int)}.
     *
     * <p>Method under test: {@link AccountImp#deleteAccount(int)}
     */
    @Test
    @DisplayName("Test deleteAccount(int)")
    @Tag("MaintainedByDiffblue")
    void testDeleteAccount2() {
        // Arrange
        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setAccount(new Account());
        forgotPassword.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword.setFpid(1);
        forgotPassword.setOtp(1);

        Account account = new Account();
        account.setEmail("jane.doe@example.org");
        account.setForgotPassword(forgotPassword);
        account.setId(1);
        account.setPassword("iloveyou");
        account.setRoles(new HashSet<>());
        account.setStatus("Status");
        account.setUsername("janedoe");

        ForgotPassword forgotPassword2 = new ForgotPassword();
        forgotPassword2.setAccount(account);
        forgotPassword2.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword2.setFpid(1);
        forgotPassword2.setOtp(1);

        Account account2 = new Account();
        account2.setEmail("jane.doe@example.org");
        account2.setForgotPassword(forgotPassword2);
        account2.setId(1);
        account2.setPassword("iloveyou");
        account2.setRoles(new HashSet<>());
        account2.setStatus("Status");
        account2.setUsername("janedoe");
        Optional<Account> ofResult = Optional.of(account2);

        AccountRepo accountRepo = mock(AccountRepo.class);
        when(accountRepo.save(Mockito.<Account>any()))
                .thenThrow(new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        when(accountRepo.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        RoleRepo roleRepo = mock(RoleRepo.class);
        AccountMapperImpl accountMapper = new AccountMapperImpl();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationImp authenticationImp =
                new AuthenticationImp(mock(InvalidDateTokenRepo.class), mock(AccountRepo.class));

        AccountImp accountImp =
                new AccountImp(accountRepo, roleRepo, accountMapper, passwordEncoder, authenticationImp);

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp.deleteAccount(1));
        verify(accountRepo).findById(1);
        verify(accountRepo).save(isA(Account.class));
    }

    /**
     * Test {@link AccountImp#deleteAccount(int)}.
     *
     * <ul>
     *   <li>Given {@link AccountRepo} {@link AccountRepo#findById(Object)} return empty.
     *   <li>Then throw {@link AppException}.
     * </ul>
     *
     * <p>Method under test: {@link AccountImp#deleteAccount(int)}
     */
    @Test
    @DisplayName(
            "Test deleteAccount(int); given AccountRepo findById(Object) return empty; then throw AppException")
    @Tag("MaintainedByDiffblue")
    void testDeleteAccount_givenAccountRepoFindByIdReturnEmpty_thenThrowAppException() {
        // Arrange
        AccountRepo accountRepo = mock(AccountRepo.class);
        Optional<Account> emptyResult = Optional.empty();
        when(accountRepo.findById(Mockito.<Integer>any())).thenReturn(emptyResult);
        RoleRepo roleRepo = mock(RoleRepo.class);
        AccountMapperImpl accountMapper = new AccountMapperImpl();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationImp authenticationImp =
                new AuthenticationImp(mock(InvalidDateTokenRepo.class), mock(AccountRepo.class));

        AccountImp accountImp =
                new AccountImp(accountRepo, roleRepo, accountMapper, passwordEncoder, authenticationImp);

        // Act and Assert
        assertThrows(AppException.class, () -> accountImp.deleteAccount(1));
        verify(accountRepo).findById(1);
    }

    /**
     * Test {@link AccountImp#deleteAccount(int)}.
     *
     * <ul>
     *   <li>Given {@link AccountRepo} {@link AccountRepo#save(Object)} return {@link
     *       Account#Account()}.
     *   <li>Then calls {@link AccountRepo#save(Object)}.
     * </ul>
     *
     * <p>Method under test: {@link AccountImp#deleteAccount(int)}
     */
    @Test
    @DisplayName(
            "Test deleteAccount(int); given AccountRepo save(Object) return Account(); then calls save(Object)")
    @Tag("MaintainedByDiffblue")
    void testDeleteAccount_givenAccountRepoSaveReturnAccount_thenCallsSave() {
        // Arrange
        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setAccount(new Account());
        forgotPassword.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword.setFpid(1);
        forgotPassword.setOtp(1);

        Account account = new Account();
        account.setEmail("jane.doe@example.org");
        account.setForgotPassword(forgotPassword);
        account.setId(1);
        account.setPassword("iloveyou");
        account.setRoles(new HashSet<>());
        account.setStatus("Status");
        account.setUsername("janedoe");

        ForgotPassword forgotPassword2 = new ForgotPassword();
        forgotPassword2.setAccount(account);
        forgotPassword2.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword2.setFpid(1);
        forgotPassword2.setOtp(1);

        Account account2 = new Account();
        account2.setEmail("jane.doe@example.org");
        account2.setForgotPassword(forgotPassword2);
        account2.setId(1);
        account2.setPassword("iloveyou");
        account2.setRoles(new HashSet<>());
        account2.setStatus("Status");
        account2.setUsername("janedoe");
        Optional<Account> ofResult = Optional.of(account2);

        Account account3 = new Account();
        account3.setEmail("jane.doe@example.org");
        account3.setForgotPassword(new ForgotPassword());
        account3.setId(1);
        account3.setPassword("iloveyou");
        account3.setRoles(new HashSet<>());
        account3.setStatus("Status");
        account3.setUsername("janedoe");

        ForgotPassword forgotPassword3 = new ForgotPassword();
        forgotPassword3.setAccount(account3);
        forgotPassword3.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword3.setFpid(1);
        forgotPassword3.setOtp(1);

        Account account4 = new Account();
        account4.setEmail("jane.doe@example.org");
        account4.setForgotPassword(forgotPassword3);
        account4.setId(1);
        account4.setPassword("iloveyou");
        account4.setRoles(new HashSet<>());
        account4.setStatus("Status");
        account4.setUsername("janedoe");

        ForgotPassword forgotPassword4 = new ForgotPassword();
        forgotPassword4.setAccount(account4);
        forgotPassword4.setExpiredAt(
                Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));
        forgotPassword4.setFpid(1);
        forgotPassword4.setOtp(1);

        Account account5 = new Account();
        account5.setEmail("jane.doe@example.org");
        account5.setForgotPassword(forgotPassword4);
        account5.setId(1);
        account5.setPassword("iloveyou");
        account5.setRoles(new HashSet<>());
        account5.setStatus("Status");
        account5.setUsername("janedoe");

        AccountRepo accountRepo = mock(AccountRepo.class);
        when(accountRepo.save(Mockito.<Account>any())).thenReturn(account5);
        when(accountRepo.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        RoleRepo roleRepo = mock(RoleRepo.class);
        AccountMapperImpl accountMapper = new AccountMapperImpl();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationImp authenticationImp =
                new AuthenticationImp(mock(InvalidDateTokenRepo.class), mock(AccountRepo.class));

        AccountImp accountImp =
                new AccountImp(accountRepo, roleRepo, accountMapper, passwordEncoder, authenticationImp);

        // Act
        accountImp.deleteAccount(1);

        // Assert
        verify(accountRepo).findById(1);
        verify(accountRepo).save(isA(Account.class));
    }
}
