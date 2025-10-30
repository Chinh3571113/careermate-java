package com.fpt.careermate.services.account_services.service.impl;

import com.fpt.careermate.services.account_services.service.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.account_services.service.dto.request.AccountUpdateRequest;
import com.fpt.careermate.services.account_services.service.dto.response.AccountResponse;
import com.fpt.careermate.common.response.PageResponse;
import org.springframework.data.domain.Pageable;


public interface AccountService {
    AccountResponse createAccount(AccountCreationRequest request) ;
    PageResponse<AccountResponse> getAccounts(Pageable pageable);
    void deleteAccount(int id);
}
