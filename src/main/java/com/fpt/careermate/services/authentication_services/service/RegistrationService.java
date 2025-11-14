package com.fpt.careermate.services.authentication_services.service;

import com.fpt.careermate.common.constant.PredefineRole;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.util.UrlValidator;
import com.fpt.careermate.common.util.MailBody;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.authentication_services.domain.Role;
import com.fpt.careermate.services.authentication_services.repository.RoleRepo;
import com.fpt.careermate.services.authentication_services.service.dto.request.RecruiterRegistrationRequest;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import com.fpt.careermate.services.email_services.service.impl.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RegistrationService {

    AccountRepo accountRepo;
    RecruiterRepo recruiterRepo;
    RoleRepo roleRepo;
    PasswordEncoder passwordEncoder;
    UrlValidator urlValidator;
    EmailService emailService;

    /**
     * Register a new recruiter account with organization info
     * Creates account with RECRUITER role and PENDING status
     * Admin approval changes status from PENDING to ACTIVE (not role change)
     */
    @Transactional
    public Account registerRecruiter(RecruiterRegistrationRequest request) {
        // Check if email already exists with any role (prevent role conflicts)
        accountRepo.findByEmail(request.getEmail()).ifPresent(existingAccount -> {
            // Check if account has a different role
            boolean hasRecruiterRole = existingAccount.getRoles().stream()
                    .anyMatch(role -> PredefineRole.RECRUITER_ROLE.equalsIgnoreCase(role.getName()));

            if (hasRecruiterRole) {
                throw new AppException(ErrorCode.DUPLICATE_EMAIL);
            } else {
                // Account exists with CANDIDATE role - cannot register as RECRUITER
                throw new AppException(ErrorCode.ROLE_CONFLICT);
            }
        });

        // Validate organization info
        validateOrganizationInfo(request.getOrganizationInfo());

        // Get RECRUITER role (account will be RECRUITER from the start)
        Role recruiterRole = roleRepo.findByName(PredefineRole.RECRUITER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // Create account with RECRUITER role and PENDING status
        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status("PENDING") // Status is PENDING, waiting for admin approval
                .roles(new HashSet<>(Set.of(recruiterRole)))
                .build();

        Account savedAccount = accountRepo.save(account);

        // Create recruiter profile
        Recruiter recruiter = createRecruiterProfile(savedAccount, request.getOrganizationInfo());
        recruiterRepo.save(recruiter);

        log.info("Recruiter account created with PENDING status: {}", savedAccount.getEmail());

        return savedAccount;
    }

    /**
     * Complete recruiter profile for Google OAuth users
     * Account already has RECRUITER role and PENDING status from OAuth handler
     * This method only creates the Recruiter entity with organization info
     */
    @Transactional
    public Recruiter completeRecruiterProfileForOAuth(String email,
            RecruiterRegistrationRequest.OrganizationInfo orgInfo) {
        // Find existing account (already has RECRUITER role and PENDING status from
        // OAuth)
        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Verify account has RECRUITER role (should have been set by OAuth handler)
        boolean hasRecruiterRole = account.getRoles().stream()
                .anyMatch(role -> PredefineRole.RECRUITER_ROLE.equalsIgnoreCase(role.getName()));

        if (!hasRecruiterRole) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check if recruiter profile already exists
        if (recruiterRepo.findByAccount_Id(account.getId()).isPresent()) {
            throw new AppException(ErrorCode.RECRUITER_ALREADY_EXISTS);
        }

        // Validate organization info
        validateOrganizationInfo(orgInfo);

        // Create recruiter profile (account already has correct role and status)
        Recruiter recruiter = createRecruiterProfile(account, orgInfo);
        Recruiter savedRecruiter = recruiterRepo.save(recruiter);

        log.info("Recruiter profile completed for OAuth user: {}", email);

        return savedRecruiter;
    }

    /**
     * Approve recruiter account (admin action)
     * Changes both account status and recruiter verification status
     */
    @Transactional
    public void approveRecruiterAccount(int recruiterId) {
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        Account account = recruiter.getAccount();

        // Check if already approved (both account status and verification status must
        // be checked)
        if ("ACTIVE".equals(account.getStatus()) && "APPROVED".equals(recruiter.getVerificationStatus())) {
            throw new AppException(ErrorCode.RECRUITER_ALREADY_APPROVED);
        }

        // Change both statuses to ACTIVE/APPROVED
        account.setStatus("ACTIVE");
        recruiter.setVerificationStatus("APPROVED");
        recruiter.setRejectionReason(null); // Clear any previous rejection reason

        accountRepo.save(account);
        recruiterRepo.save(recruiter);

        // send email notification
        try {
            String subject = "Your recruiter account has been approved";
            String text = String.format("Hello %s,\n\nYour recruiter account for %s has been approved and is now active. You can sign in and start posting jobs.\n\nBest regards,\nCareerMate Team",
                    account.getUsername(), recruiter.getCompanyName());
            emailService.sendSimpleEmail(MailBody.builder().to(account.getEmail()).subject(subject).text(text).build());
        } catch (Exception ex) {
            log.warn("Failed to send approval email to {}: {}", account.getEmail(), ex.getMessage());
        }

        log.info("Recruiter account approved. Account ID: {}, Status: PENDING → ACTIVE", account.getId());
    }

    /**
     * Reject recruiter account (admin action)
     * Marks the recruiter as REJECTED and stores the rejection reason
     * Changes account status to REJECTED to prevent login
     */
    @Transactional
    public void rejectRecruiterAccount(int recruiterId, String reason) {
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        Account account = recruiter.getAccount();

        // Check if already rejected
        if ("REJECTED".equals(recruiter.getVerificationStatus())) {
            throw new AppException(ErrorCode.RECRUITER_ALREADY_REJECTED);
        }

        // Mark recruiter as rejected and store reason
        recruiter.setVerificationStatus("REJECTED");
        recruiter.setRejectionReason(reason != null ? reason : "No reason provided");

        // Change account status to REJECTED to prevent login
        account.setStatus("REJECTED");

        recruiterRepo.save(recruiter);
        accountRepo.save(account);

        // send rejection email
        try {
            String subject = "Your recruiter application has been rejected";
            String text = String.format("Hello %s,\n\nWe reviewed your recruiter application for %s and unfortunately it has been rejected. Reason: %s\n\nIf you believe this is a mistake, please contact support.\n\nBest regards,\nCareerMate Team",
                    account.getUsername(), recruiter.getCompanyName(), recruiter.getRejectionReason());
            emailService.sendSimpleEmail(MailBody.builder().to(account.getEmail()).subject(subject).text(text).build());
        } catch (Exception ex) {
            log.warn("Failed to send rejection email to {}: {}", account.getEmail(), ex.getMessage());
        }

        log.info("Recruiter account rejected. Account ID: {}, Email: {}, Status: {} → REJECTED, Reason: {}",
                account.getId(), account.getEmail(), account.getStatus(), reason);
    }

    /**
     * Ban a recruiter account (admin action)
     * Changes both account status and recruiter verification status to
     * BANNED/REJECTED
     */
    @Transactional
    public void banRecruiterAccount(int accountId, String reason) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Find recruiter profile if exists
        recruiterRepo.findByAccount_Id(accountId).ifPresent(recruiter -> {
            recruiter.setVerificationStatus("REJECTED");
            recruiter.setRejectionReason(reason != null ? reason : "Account banned by admin");
            recruiterRepo.save(recruiter);
        });

        account.setStatus("BANNED");
        accountRepo.save(account);

        // send ban email
        try {
            String subject = "Your account has been banned";
            String text = String.format("Hello %s,\n\nYour account has been banned. Reason: %s\n\nIf you want to appeal, please contact support.",
                    account.getUsername(), reason != null ? reason : "No reason provided");
            emailService.sendSimpleEmail(MailBody.builder().to(account.getEmail()).subject(subject).text(text).build());
        } catch (Exception ex) {
            log.warn("Failed to send ban email to {}: {}", account.getEmail(), ex.getMessage());
        }

        log.info("Account banned. ID: {}, Reason: {}", accountId, reason);
    }

    /**
     * Unban a recruiter account (admin action)
     * Changes both account status and recruiter verification status back to
     * ACTIVE/APPROVED
     */
    @Transactional
    public void unbanRecruiterAccount(int accountId) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Find recruiter profile if exists
        recruiterRepo.findByAccount_Id(accountId).ifPresent(recruiter -> {
            recruiter.setVerificationStatus("APPROVED");
            recruiter.setRejectionReason(null); // Clear rejection reason
            recruiterRepo.save(recruiter);
        });

        account.setStatus("ACTIVE");
        accountRepo.save(account);

        // send unban email
        try {
            String subject = "Your account has been unbanned";
            String text = String.format("Hello %s,\n\nYour account has been unbanned and is now active.\n\nBest regards,\nCareerMate Team",
                    account.getUsername());
            emailService.sendSimpleEmail(MailBody.builder().to(account.getEmail()).subject(subject).text(text).build());
        } catch (Exception ex) {
            log.warn("Failed to send unban email to {}: {}", account.getEmail(), ex.getMessage());
        }

        log.info("Account unbanned. ID: {}", accountId);
    }

    // Helper methods

    private void validateOrganizationInfo(RecruiterRegistrationRequest.OrganizationInfo orgInfo) {
        // Validate website
        if (!urlValidator.isWebsiteReachable(orgInfo.getWebsite())) {
            throw new AppException(ErrorCode.INVALID_WEBSITE);
        }

        // Validate logo URL if provided
        if (orgInfo.getLogoUrl() != null && !orgInfo.getLogoUrl().isEmpty()) {
            if (!urlValidator.isImageUrlValid(orgInfo.getLogoUrl())) {
                throw new AppException(ErrorCode.INVALID_LOGO_URL);
            }
        }
    }

    private Recruiter createRecruiterProfile(Account account, RecruiterRegistrationRequest.OrganizationInfo orgInfo) {
        return Recruiter.builder()
                .account(account)
                .companyName(orgInfo.getCompanyName())
                .website(orgInfo.getWebsite())
                .logoUrl(orgInfo.getLogoUrl() != null ? orgInfo.getLogoUrl() : "https://via.placeholder.com/150")
                .about(orgInfo.getAbout())
                .rating(0.0f)
                .companyEmail(orgInfo.getCompanyEmail())
                .contactPerson(orgInfo.getContactPerson())
                .phoneNumber(orgInfo.getPhoneNumber())
                .companyAddress(orgInfo.getCompanyAddress())
                .verificationStatus("PENDING")
                .build();
    }
}

