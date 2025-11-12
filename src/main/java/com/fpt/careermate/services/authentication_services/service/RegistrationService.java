package com.fpt.careermate.services.authentication_services.service;

import com.fpt.careermate.common.constant.PredefineRole;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.util.UrlValidator;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.authentication_services.domain.Role;
import com.fpt.careermate.services.authentication_services.repository.RoleRepo;
import com.fpt.careermate.services.authentication_services.service.dto.request.RecruiterRegistrationRequest;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;

import com.fpt.careermate.services.kafka.producer.NotificationProducer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    NotificationProducer notificationProducer;

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

        // Send notification to admin about new recruiter registration
        sendRecruiterRegistrationNotificationToAdmin(savedAccount, recruiter);

        return savedAccount;
    }

    /**
     * Complete recruiter profile for Google OAuth users
     * Account already has RECRUITER role and PENDING status from OAuth handler
     * This method only creates the Recruiter entity with organization info
     */
    @Transactional
    public Recruiter completeRecruiterProfileForOAuth(String email, RecruiterRegistrationRequest.OrganizationInfo orgInfo) {
        // Find existing account (already has RECRUITER role and PENDING status from OAuth)
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

        // Send notification to admin about new recruiter registration
        sendRecruiterRegistrationNotificationToAdmin(account, savedRecruiter);

        return savedRecruiter;
    }

    /**
     * Approve recruiter account (admin action)
     * Changes account status from PENDING to ACTIVE
     */
    @Transactional
    public void approveRecruiterAccount(int recruiterId) {
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        Account account = recruiter.getAccount();

        // Check if already active
        if ("ACTIVE".equals(account.getStatus())) {
            throw new AppException(ErrorCode.RECRUITER_ALREADY_APPROVED);
        }

        // Change status to ACTIVE
        account.setStatus("ACTIVE");
        recruiter.setVerificationStatus("APPROVED");

        accountRepo.save(account);
        recruiterRepo.save(recruiter);

        log.info("Recruiter account approved. Account ID: {}, Status: PENDING → ACTIVE", account.getId());

        // Send approval notification to recruiter
        sendRecruiterApprovalNotification(account, recruiter);
    }

    /**
     * Reject recruiter account (admin action)
     * Deletes both account and recruiter profile
     */
    @Transactional
    public void rejectRecruiterAccount(int recruiterId, String reason) {
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        Account account = recruiter.getAccount();
        int accountId = account.getId();
        String email = account.getEmail();

        // Send rejection notification BEFORE deletion
        sendRecruiterRejectionNotification(account, recruiter, reason);

        // Delete recruiter profile first (due to foreign key)
        recruiterRepo.delete(recruiter);

        // Delete account
        accountRepo.delete(account);

        log.info("Recruiter account rejected and deleted. Account ID: {}, Email: {}, Reason: {}",
                accountId, email, reason);
    }

    /**
     * Ban a recruiter account (admin action)
     */
    @Transactional
    public void banRecruiterAccount(int accountId, String reason) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        account.setStatus("BANNED");
        accountRepo.save(account);

        log.info("Account banned. ID: {}, Reason: {}", accountId, reason);
    }

    /**
     * Unban a recruiter account (admin action)
     */
    @Transactional
    public void unbanRecruiterAccount(int accountId) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        account.setStatus("ACTIVE");
        accountRepo.save(account);

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

    /**
     * Send notification to admin when a new recruiter registers
     */
    private void sendRecruiterRegistrationNotificationToAdmin(Account account, Recruiter recruiter) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("accountId", account.getId());
            metadata.put("recruiterId", recruiter.getId());
            metadata.put("companyName", recruiter.getCompanyName());
            metadata.put("email", account.getEmail());
            metadata.put("username", account.getUsername());
            metadata.put("actionType", "RECRUITER_REGISTRATION");
            metadata.put("actionUrl", "/api/admin/recruiters/" + recruiter.getId());

            NotificationEvent event = NotificationEvent.builder()
                    .eventType(NotificationEvent.EventType.PROFILE_VERIFICATION.name())
                    .recipientId("ADMIN")
                    .recipientEmail("admin@careermate.com")
                    .title("New Recruiter Registration")
                    .subject("New Recruiter Pending Approval")
                    .message(String.format(
                            "A new recruiter has registered and needs verification.\n\n" +
                            "Company: %s\n" +
                            "Email: %s\n" +
                            "Username: %s\n" +
                            "Contact: %s\n\n" +
                            "Please review and approve/reject this registration.",
                            recruiter.getCompanyName(),
                            account.getEmail(),
                            account.getUsername(),
                            recruiter.getContactPerson()
                    ))
                    .category("ADMIN_ACTION_REQUIRED")
                    .metadata(metadata)
                    .priority(1) // High priority
                    .build();

            notificationProducer.sendAdminNotification(event);
            log.info("✅ Admin notification sent for new recruiter registration: {}", account.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send admin notification for recruiter registration: {}", account.getEmail(), e);
            // Don't throw exception - notification failure should not break registration
        }
    }

    /**
     * Send notification to recruiter when their account is approved
     */
    private void sendRecruiterApprovalNotification(Account account, Recruiter recruiter) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("accountId", account.getId());
            metadata.put("recruiterId", recruiter.getId());
            metadata.put("companyName", recruiter.getCompanyName());
            metadata.put("status", "APPROVED");

            NotificationEvent event = NotificationEvent.builder()
                    .eventType(NotificationEvent.EventType.ACCOUNT_APPROVED.name())
                    .recipientId(String.valueOf(account.getId()))
                    .recipientEmail(account.getEmail())
                    .title("Account Approved")
                    .subject("Your Recruiter Account Has Been Approved")
                    .message(String.format(
                            "Congratulations! Your recruiter account for %s has been approved.\n\n" +
                            "You can now access all recruiter features:\n" +
                            "- Post job openings\n" +
                            "- Manage applications\n" +
                            "- Search for candidates\n\n" +
                            "Welcome to CareerMate!",
                            recruiter.getCompanyName()
                    ))
                    .category("ACCOUNT_STATUS")
                    .metadata(metadata)
                    .priority(1) // High priority
                    .build();

            notificationProducer.sendRecruiterNotification(event);
            log.info("✅ Approval notification sent to recruiter: {}", account.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send approval notification to recruiter: {}", account.getEmail(), e);
        }
    }

    /**
     * Send notification to recruiter when their account is rejected
     */
    private void sendRecruiterRejectionNotification(Account account, Recruiter recruiter, String reason) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("accountId", account.getId());
            metadata.put("recruiterId", recruiter.getId());
            metadata.put("companyName", recruiter.getCompanyName());
            metadata.put("rejectionReason", reason != null ? reason : "No reason provided");
            metadata.put("status", "REJECTED");

            String reasonText = reason != null && !reason.isEmpty()
                    ? reason
                    : "Your application did not meet our verification requirements.";

            NotificationEvent event = NotificationEvent.builder()
                    .eventType(NotificationEvent.EventType.ACCOUNT_REJECTED.name())
                    .recipientId(String.valueOf(account.getId()))
                    .recipientEmail(account.getEmail())
                    .title("Account Registration Rejected")
                    .subject("Your Recruiter Account Application")
                    .message(String.format(
                            "We regret to inform you that your recruiter account application for %s has been rejected.\n\n" +
                            "Reason: %s\n\n" +
                            "You can create a new account with updated information if you wish to reapply.\n\n" +
                            "If you have questions, please contact our support team.",
                            recruiter.getCompanyName(),
                            reasonText
                    ))
                    .category("ACCOUNT_STATUS")
                    .metadata(metadata)
                    .priority(1) // High priority
                    .build();

            notificationProducer.sendRecruiterNotification(event);
            log.info("✅ Rejection notification sent to recruiter: {}", account.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send rejection notification to recruiter: {}", account.getEmail(), e);
        }
    }
}


