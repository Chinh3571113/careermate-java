package com.fpt.careermate.common.util;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for handling security context and user identification.
 * Bridges the gap between JWT authentication (email-based) and database entities (ID-based).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtil {

    private final AccountRepo accountRepo;

    /**
     * Get the current authenticated user's email from JWT token.
     * This is what's stored in the JWT subject field.
     *
     * @return User's email address
     * @throws AppException if not authenticated
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() 
                || authentication.getName().equals("anonymousUser")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName(); // Returns email from JWT subject
    }

    /**
     * Get the current authenticated user's database ID.
     * Looks up the Account by email and returns the integer ID.
     *
     * @return User's database ID
     * @throws AppException if not authenticated or account not found
     */
    public Integer getCurrentUserId() {
        String email = getCurrentUserEmail();
        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return account.getId();
    }

    /**
     * Get the current authenticated Account entity.
     *
     * @return Full Account entity
     * @throws AppException if not authenticated or account not found
     */
    public Account getCurrentAccount() {
        String email = getCurrentUserEmail();
        return accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Convert user email to database ID.
     *
     * @param email User's email address
     * @return User's database ID
     * @throws AppException if account not found
     */
    public Integer getIdByEmail(String email) {
        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return account.getId();
    }

    /**
     * Convert user database ID to email.
     *
     * @param userId User's database ID
     * @return User's email address
     * @throws AppException if account not found
     */
    public String getEmailById(Integer userId) {
        Account account = accountRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return account.getEmail();
    }

    /**
     * Check if current user owns the given account ID.
     *
     * @param accountId Account ID to check
     * @return true if current user owns this account
     */
    public boolean isCurrentUser(Integer accountId) {
        try {
            Integer currentUserId = getCurrentUserId();
            return currentUserId.equals(accountId);
        } catch (Exception e) {
            return false;
        }
    }
}
