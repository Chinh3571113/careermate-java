package com.fpt.careermate.services.account_services.service.impl;

import com.fpt.careermate.services.account_services.service.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.account_services.service.dto.response.AccountResponse;
import com.fpt.careermate.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface AccountService {
    AccountResponse createAccount(AccountCreationRequest request) ;
    PageResponse<AccountResponse> getAccounts(Pageable pageable);
    PageResponse<AccountResponse> searchAccounts(List<String> roles, List<String> statuses, String keyword, Pageable pageable);
    AccountResponse getAccountById(int id);
    void deleteAccount(int id);
    AccountResponse getCurrentUser();
}
