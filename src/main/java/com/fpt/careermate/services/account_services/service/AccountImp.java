package com.fpt.careermate.services.account_services.service;

import com.fpt.careermate.common.constant.PredefineRole;
import com.fpt.careermate.common.constant.StatusAccount;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.domain.Role;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.authentication_services.repository.RoleRepo;
import com.fpt.careermate.services.account_services.service.impl.AccountService;
import com.fpt.careermate.services.account_services.service.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.account_services.service.dto.request.AccountUpdateRequest;
import com.fpt.careermate.services.account_services.service.dto.response.AccountResponse;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.account_services.service.mapper.AccountMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountImp implements AccountService {
    AccountRepo accountRepo;
    RoleRepo roleRepo;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;
    AuthenticationImp authenticationImp;

    @Override
    public AccountResponse createAccount(AccountCreationRequest request) {
        if (accountRepo.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.DUPLICATE_EMAIL);


        Account user = accountMapper.toAccount(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepo.findById(PredefineRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        user.setStatus("ACTIVE");

        user = accountRepo.save(user);

        return accountMapper.toAccountResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public PageResponse<AccountResponse> getAccounts(Pageable pageable) {
        Page<Account> accountResponses = accountRepo.findAll(pageable);
        return new PageResponse<>(
                accountResponses.getContent()
                        .stream()
                        .map(accountMapper::toAccountResponse)
                        .toList(),
                accountResponses.getNumber(),
                accountResponses.getSize(),
                accountResponses.getTotalElements(),
                accountResponses.getTotalPages()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void deleteAccount(int id) {
        Account account = accountRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        account.setStatus(StatusAccount.INACTIVE);
        accountRepo.save(account);
    }


}
