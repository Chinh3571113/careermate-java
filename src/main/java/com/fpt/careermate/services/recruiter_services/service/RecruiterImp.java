package com.fpt.careermate.services.recruiter_services.service;

import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.domain.RecruiterProfileUpdateRequest;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterProfileUpdateRequestRepo;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterUpdateRequest;
import com.fpt.careermate.services.recruiter_services.service.dto.response.NewRecruiterResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterApprovalResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterProfileResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterUpdateRequestResponse;
import com.fpt.careermate.services.recruiter_services.service.impl.RecruiterService;
import com.fpt.careermate.services.recruiter_services.service.mapper.RecruiterMapper;
import com.fpt.careermate.services.email_services.service.impl.EmailService;
import com.fpt.careermate.common.util.MailBody;
import com.fpt.careermate.common.util.UrlValidator;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterImp implements RecruiterService {

    RecruiterRepo recruiterRepo;
    RecruiterProfileUpdateRequestRepo updateRequestRepo;
    RecruiterMapper recruiterMapper;
    UrlValidator urlValidator;
    AuthenticationImp authenticationImp;
    EmailService emailService;

    // Method for authenticated users to add their recruiter/company profile
    // Used by existing accounts that want to add organization information
    @Override
    public NewRecruiterResponse createRecruiter(RecruiterCreationRequest request) {
        // Check website
        if (!urlValidator.isWebsiteReachable(request.getWebsite()))
            throw new AppException(ErrorCode.INVALID_WEBSITE);

        // Check logo URL only if provided (optional field)
        if (request.getLogoUrl() != null && !request.getLogoUrl().isEmpty()) {
            if (!urlValidator.isImageUrlValid(request.getLogoUrl())) {
                throw new AppException(ErrorCode.INVALID_LOGO_URL);
            }
        }

        // Check duplicate recruiter for the account
        recruiterRepo.findByAccount_Id(authenticationImp.findByEmail().getId())
                .ifPresent(recruiter -> {
                    throw new AppException(ErrorCode.RECRUITER_ALREADY_EXISTS);
                });

        Recruiter recruiter = recruiterMapper.toRecruiter(request);
        recruiter.setAccount(authenticationImp.findByEmail());
        recruiter.setRating(0.0f); // Set default rating to avoid null value error

        // Set default logo if not provided
        if (recruiter.getLogoUrl() == null || recruiter.getLogoUrl().isEmpty()) {
            recruiter.setLogoUrl("https://via.placeholder.com/150");
        }

        // save to db, convert to response and return
        return recruiterMapper.toNewRecruiterResponse(recruiterRepo.save(recruiter));
    }

    @Override
    public List<RecruiterApprovalResponse> getPendingRecruiters() {
        // Get all recruiters with PENDING status (waiting for admin approval)
        // Use paginated query to avoid loading all data - fetch first 1000 records max
        Pageable pageable = PageRequest.of(0, 1000, Sort.by("id").descending());
        Page<Recruiter> recruiterPage = recruiterRepo.findByAccount_Status("PENDING", pageable);

        return recruiterPage.getContent().stream()
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecruiterApprovalResponse> getAllRecruiters() {
        // Get all recruiters with ACTIVE status (approved by admin)
        // Use paginated query to avoid loading all data - fetch first 1000 records max
        Pageable pageable = PageRequest.of(0, 1000, Sort.by("id").descending());
        Page<Recruiter> recruiterPage = recruiterRepo.findByAccount_Status("ACTIVE", pageable);

        return recruiterPage.getContent().stream()
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<RecruiterApprovalResponse> getRecruitersByStatus(String status, int page, int size,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Recruiter> recruiterPage;
        // Handle null or empty status - get all recruiters
        if (status == null || status.trim().isEmpty()) {
            recruiterPage = recruiterRepo.findAll(pageable);
        } else {
            recruiterPage = recruiterRepo.findByAccount_Status(status.trim(), pageable);
        }

        List<RecruiterApprovalResponse> content = recruiterPage.getContent().stream()
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                recruiterPage.getNumber(),
                recruiterPage.getSize(),
                recruiterPage.getTotalElements(),
                recruiterPage.getTotalPages());
    }

    @Override
    public PageResponse<RecruiterApprovalResponse> searchRecruiters(String status, String search, int page, int size,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Handle null parameters - convert to empty string for query
        String safeStatus = (status == null || status.trim().isEmpty()) ? "" : status.trim();
        String safeSearch = (search == null || search.trim().isEmpty()) ? "" : search.trim();

        Page<Recruiter> recruiterPage = recruiterRepo.searchRecruiters(safeStatus, safeSearch, pageable);

        List<RecruiterApprovalResponse> content = recruiterPage.getContent().stream()
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                recruiterPage.getNumber(),
                recruiterPage.getSize(),
                recruiterPage.getTotalElements(),
                recruiterPage.getTotalPages());
    }

    @Override
    public RecruiterApprovalResponse getRecruiterById(int recruiterId) {
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));
        return mapToApprovalResponse(recruiter);
    }

    @Override
    public RecruiterApprovalResponse getMyRecruiterProfile() {
        // Get current authenticated user's account
        var currentAccount = authenticationImp.findByEmail();

        // Find recruiter profile by account
        Recruiter recruiter = recruiterRepo.findByAccount_Id(currentAccount.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        return mapToApprovalResponse(recruiter);
    }

    @Override
    public void updateOrganizationInfo(com.fpt.careermate.services.authentication_services.service.dto.request.RecruiterRegistrationRequest.OrganizationInfo orgInfo) {
        // Get current authenticated user's account
        var currentAccount = authenticationImp.findByEmail();

        // Check if account is BANNED - banned accounts cannot update
        if ("BANNED".equals(currentAccount.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_BANNED);
        }

        // Find recruiter profile
        Recruiter recruiter = recruiterRepo.findByAccount_Id(currentAccount.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        // Check if account is REJECTED - only rejected accounts can update and resubmit
        if (!"REJECTED".equals(currentAccount.getStatus())) {
            throw new AppException(ErrorCode.CANNOT_UPDATE_NON_REJECTED_PROFILE);
        }

        // Validate organization info
        if (!urlValidator.isWebsiteReachable(orgInfo.getWebsite())) {
            throw new AppException(ErrorCode.INVALID_WEBSITE);
        }

        // Validate logo URL if provided
        if (orgInfo.getLogoUrl() != null && !orgInfo.getLogoUrl().isEmpty()) {
            if (!urlValidator.isImageUrlValid(orgInfo.getLogoUrl())) {
                throw new AppException(ErrorCode.INVALID_LOGO_URL);
            }
        }

        // Update recruiter profile fields
        recruiter.setCompanyName(orgInfo.getCompanyName());
        recruiter.setWebsite(orgInfo.getWebsite());
        recruiter.setLogoUrl(orgInfo.getLogoUrl() != null ? orgInfo.getLogoUrl() : recruiter.getLogoUrl());
        recruiter.setAbout(orgInfo.getAbout());
        recruiter.setContactPerson(orgInfo.getContactPerson());
        recruiter.setPhoneNumber(orgInfo.getPhoneNumber());
        recruiter.setCompanyAddress(orgInfo.getCompanyAddress());

        // Reset verification status to PENDING for admin review
        recruiter.setVerificationStatus("PENDING");
        recruiter.setRejectionReason(null); // Clear previous rejection reason

        // Change account status back to PENDING
        currentAccount.setStatus("PENDING");

        recruiterRepo.save(recruiter);

        log.info("Recruiter organization info updated. Account ID: {}, Status: REJECTED → PENDING", currentAccount.getId());
    }

    // ========== RECRUITER PROFILE MANAGEMENT ==========

    @Override
    public RecruiterProfileResponse getMyProfile() {
        Recruiter recruiter = getAuthenticatedRecruiter();

        RecruiterProfileResponse response = RecruiterProfileResponse.builder()
                .recruiterId(recruiter.getId())
                .accountId(recruiter.getAccount().getId())
                .email(recruiter.getAccount().getEmail())
                .username(recruiter.getAccount().getUsername())
                .companyName(recruiter.getCompanyName())
                .website(recruiter.getWebsite())
                .logoUrl(recruiter.getLogoUrl())
                .about(recruiter.getAbout())
                .rating(recruiter.getRating())
                .companyEmail(recruiter.getCompanyEmail())
                .contactPerson(recruiter.getContactPerson())
                .phoneNumber(recruiter.getPhoneNumber())
                .companyAddress(recruiter.getCompanyAddress())
                .verificationStatus(recruiter.getVerificationStatus())
                .rejectionReason(recruiter.getRejectionReason())
                .build();

        // Check for pending update request
        updateRequestRepo.findByRecruiterIdAndStatus(recruiter.getId(), "PENDING")
                .ifPresent(updateRequest -> {
                    response.setHasPendingUpdate(true);
                    response.setPendingUpdateRequest(mapToUpdateRequestResponse(updateRequest));
                });

        return response;
    }

    @Override
    @Transactional
    public RecruiterUpdateRequestResponse requestProfileUpdate(RecruiterUpdateRequest request) {
        Recruiter recruiter = getAuthenticatedRecruiter();

        // Check if there's already a pending request
        if (updateRequestRepo.existsByRecruiterIdAndStatus(recruiter.getId(), "PENDING")) {
            throw new AppException(ErrorCode.PENDING_UPDATE_REQUEST_EXISTS);
        }

        // Validate URLs if provided
        if (request.getWebsite() != null && !urlValidator.isWebsiteReachable(request.getWebsite())) {
            throw new AppException(ErrorCode.INVALID_WEBSITE);
        }
        if (request.getLogoUrl() != null && !urlValidator.isImageUrlValid(request.getLogoUrl())) {
            throw new AppException(ErrorCode.INVALID_LOGO_URL);
        }

        RecruiterProfileUpdateRequest updateRequest = RecruiterProfileUpdateRequest.builder()
                .recruiter(recruiter)
                .newCompanyName(request.getCompanyName())
                .newWebsite(request.getWebsite())
                .newLogoUrl(request.getLogoUrl())
                .newAbout(request.getAbout())
                .newCompanyEmail(request.getCompanyEmail())
                .newContactPerson(request.getContactPerson())
                .newPhoneNumber(request.getPhoneNumber())
                .newCompanyAddress(request.getCompanyAddress())
                .status("PENDING")
                .build();

        updateRequestRepo.save(updateRequest);
        log.info("Profile update request created for recruiter ID: {}", recruiter.getId());

        return mapToUpdateRequestResponse(updateRequest);
    }

    @Override
    public List<RecruiterUpdateRequestResponse> getMyUpdateRequests() {
        Recruiter recruiter = getAuthenticatedRecruiter();
        return updateRequestRepo.findByRecruiterId(recruiter.getId()).stream()
                .map(this::mapToUpdateRequestResponse)
                .collect(Collectors.toList());
    }

    // ========== ADMIN - UPDATE REQUEST MANAGEMENT ==========

    @Override
    public PageResponse<RecruiterUpdateRequestResponse> getAllUpdateRequests(String status, int page, int size,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<RecruiterProfileUpdateRequest> requestPage;
        if (status == null || status.trim().isEmpty()) {
            requestPage = updateRequestRepo.findAll(pageable);
        } else {
            requestPage = updateRequestRepo.findByStatus(status.trim(), pageable);
        }

        List<RecruiterUpdateRequestResponse> content = requestPage.getContent().stream()
                .map(this::mapToUpdateRequestResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                requestPage.getNumber(),
                requestPage.getSize(),
                requestPage.getTotalElements(),
                requestPage.getTotalPages());
    }

    @Override
    public PageResponse<RecruiterUpdateRequestResponse> searchUpdateRequests(String status, String search, int page,
            int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String safeStatus = (status == null || status.trim().isEmpty()) ? "" : status.trim();
        String safeSearch = (search == null || search.trim().isEmpty()) ? "" : search.trim();

        Page<RecruiterProfileUpdateRequest> requestPage = updateRequestRepo.searchUpdateRequests(safeStatus, safeSearch,
                pageable);

        List<RecruiterUpdateRequestResponse> content = requestPage.getContent().stream()
                .map(this::mapToUpdateRequestResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                requestPage.getNumber(),
                requestPage.getSize(),
                requestPage.getTotalElements(),
                requestPage.getTotalPages());
    }

    @Override
    public RecruiterUpdateRequestResponse getUpdateRequestById(int requestId) {
        RecruiterProfileUpdateRequest updateRequest = updateRequestRepo.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.UPDATE_REQUEST_NOT_FOUND));
        return mapToUpdateRequestResponse(updateRequest);
    }

    @Override
    @Transactional
    public void approveUpdateRequest(int requestId, String adminNote) {
        RecruiterProfileUpdateRequest updateRequest = updateRequestRepo.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.UPDATE_REQUEST_NOT_FOUND));

        if (!"PENDING".equals(updateRequest.getStatus())) {
            throw new AppException(ErrorCode.UPDATE_REQUEST_ALREADY_PROCESSED);
        }

        Recruiter recruiter = updateRequest.getRecruiter();

        // Apply changes to recruiter profile
        if (updateRequest.getNewCompanyName() != null) {
            recruiter.setCompanyName(updateRequest.getNewCompanyName());
        }
        if (updateRequest.getNewWebsite() != null) {
            recruiter.setWebsite(updateRequest.getNewWebsite());
        }
        if (updateRequest.getNewLogoUrl() != null) {
            recruiter.setLogoUrl(updateRequest.getNewLogoUrl());
        }
        if (updateRequest.getNewAbout() != null) {
            recruiter.setAbout(updateRequest.getNewAbout());
        }
        if (updateRequest.getNewCompanyEmail() != null) {
            recruiter.setCompanyEmail(updateRequest.getNewCompanyEmail());
        }
        if (updateRequest.getNewContactPerson() != null) {
            recruiter.setContactPerson(updateRequest.getNewContactPerson());
        }
        if (updateRequest.getNewPhoneNumber() != null) {
            recruiter.setPhoneNumber(updateRequest.getNewPhoneNumber());
        }
        if (updateRequest.getNewCompanyAddress() != null) {
            recruiter.setCompanyAddress(updateRequest.getNewCompanyAddress());
        }

        recruiterRepo.save(recruiter);

        // Update request status
        updateRequest.setStatus("APPROVED");
        updateRequest.setAdminNote(adminNote);
        updateRequest.setReviewedAt(LocalDateTime.now());
        updateRequestRepo.save(updateRequest);

        log.info("Profile update request {} approved for recruiter {}", requestId, recruiter.getId());

        // Send notification to recruiter
        sendProfileUpdateApprovedEmail(recruiter, updateRequest, adminNote);
    }

    @Override
    @Transactional
    public void rejectUpdateRequest(int requestId, String rejectionReason) {
        RecruiterProfileUpdateRequest updateRequest = updateRequestRepo.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.UPDATE_REQUEST_NOT_FOUND));

        if (!"PENDING".equals(updateRequest.getStatus())) {
            throw new AppException(ErrorCode.UPDATE_REQUEST_ALREADY_PROCESSED);
        }

        updateRequest.setStatus("REJECTED");
        updateRequest.setRejectionReason(rejectionReason);
        updateRequest.setReviewedAt(LocalDateTime.now());
        updateRequestRepo.save(updateRequest);

        Recruiter recruiter = updateRequest.getRecruiter();
        log.info("Profile update request {} rejected for recruiter {}", requestId, recruiter.getId());

        // Send notification to recruiter
        sendProfileUpdateRejectedEmail(recruiter, updateRequest, rejectionReason);
    }

    // Helper methods
    private Recruiter getAuthenticatedRecruiter() {
        Recruiter recruiter = recruiterRepo.findByAccount_Id(authenticationImp.findByEmail().getId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        // Check if recruiter is verified (APPROVED status)
        if (!"APPROVED".equals(recruiter.getVerificationStatus())) {
            throw new AppException(ErrorCode.RECRUITER_NOT_VERIFIED);
        }

        return recruiter;
    }

    private RecruiterApprovalResponse mapToApprovalResponse(Recruiter recruiter) {
        RecruiterApprovalResponse response = recruiterMapper.toRecruiterApprovalResponse(recruiter);
        // Set account status (PENDING, ACTIVE, REJECTED, or BANNED)
        response.setAccountStatus(recruiter.getAccount().getStatus());
        // Role is always RECRUITER for recruiter accounts
        response.setAccountRole("RECRUITER");
        return response;
    }

    private RecruiterUpdateRequestResponse mapToUpdateRequestResponse(RecruiterProfileUpdateRequest request) {
        Recruiter recruiter = request.getRecruiter();

        return RecruiterUpdateRequestResponse.builder()
                .requestId(request.getId())
                .recruiterId(recruiter.getId())
                .recruiterEmail(recruiter.getAccount().getEmail())
                .recruiterUsername(recruiter.getAccount().getUsername())
                // Current values
                .currentCompanyName(recruiter.getCompanyName())
                .currentWebsite(recruiter.getWebsite())
                .currentLogoUrl(recruiter.getLogoUrl())
                .currentAbout(recruiter.getAbout())
                .currentCompanyEmail(recruiter.getCompanyEmail())
                .currentContactPerson(recruiter.getContactPerson())
                .currentPhoneNumber(recruiter.getPhoneNumber())
                .currentCompanyAddress(recruiter.getCompanyAddress())
                // New values
                .newCompanyName(request.getNewCompanyName())
                .newWebsite(request.getNewWebsite())
                .newLogoUrl(request.getNewLogoUrl())
                .newAbout(request.getNewAbout())
                .newCompanyEmail(request.getNewCompanyEmail())
                .newContactPerson(request.getNewContactPerson())
                .newPhoneNumber(request.getNewPhoneNumber())
                .newCompanyAddress(request.getNewCompanyAddress())
                // Status info
                .status(request.getStatus())
                .adminNote(request.getAdminNote())
                .rejectionReason(request.getRejectionReason())
                .createdAt(request.getCreatedAt())
                .reviewedAt(request.getReviewedAt())
                .build();
    }

    private void sendProfileUpdateApprovedEmail(Recruiter recruiter, RecruiterProfileUpdateRequest request,
            String adminNote) {
        try {
            String email = recruiter.getAccount().getEmail();
            String companyName = recruiter.getCompanyName();

            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Dear ").append(companyName).append(",\n\n");
            emailBody.append("Good news! Your profile update request has been approved by our admin team.\n\n");
            emailBody.append("Your profile has been updated with the new information.\n\n");

            if (adminNote != null && !adminNote.trim().isEmpty()) {
                emailBody.append("Admin Note: ").append(adminNote).append("\n\n");
            }

            emailBody.append("Thank you for keeping your profile up to date.\n\n");
            emailBody.append("Best regards,\n");
            emailBody.append("CareerMate Team");

            MailBody mailBody = MailBody.builder()
                    .to(email)
                    .subject("Profile Update Request Approved - CareerMate")
                    .text(emailBody.toString())
                    .build();

            emailService.sendSimpleEmail(mailBody);
            log.info("Approval email sent to recruiter {} at {}", recruiter.getId(), email);
        } catch (Exception e) {
            log.error("Failed to send approval email to recruiter {}: {}", recruiter.getId(), e.getMessage());
            // Don't throw exception - email failure shouldn't break the approval process
        }
    }

    private void sendProfileUpdateRejectedEmail(Recruiter recruiter, RecruiterProfileUpdateRequest request,
            String rejectionReason) {
        try {
            String email = recruiter.getAccount().getEmail();
            String companyName = recruiter.getCompanyName();

            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Dear ").append(companyName).append(",\n\n");
            emailBody.append(
                    "We regret to inform you that your profile update request has been reviewed and could not be approved.\n\n");
            emailBody.append("Reason for Rejection:\n");
            emailBody.append(rejectionReason != null ? rejectionReason : "No specific reason provided").append("\n\n");
            emailBody.append("You can submit a new update request after addressing the issues mentioned above.\n\n");
            emailBody.append("If you have any questions, please contact our support team.\n\n");
            emailBody.append("Best regards,\n");
            emailBody.append("CareerMate Team");

            MailBody mailBody = MailBody.builder()
                    .to(email)
                    .subject("Profile Update Request Rejected - CareerMate")
                    .text(emailBody.toString())
                    .build();

            emailService.sendSimpleEmail(mailBody);
            log.info("Rejection email sent to recruiter {} at {}", recruiter.getId(), email);
        } catch (Exception e) {
            log.error("Failed to send rejection email to recruiter {}: {}", recruiter.getId(), e.getMessage());
            // Don't throw exception - email failure shouldn't break the rejection process
        }
    }

}
