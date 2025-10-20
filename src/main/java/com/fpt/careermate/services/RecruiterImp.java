package com.fpt.careermate.services;

import com.fpt.careermate.constant.PredefineRole;
import com.fpt.careermate.domain.Account;
import com.fpt.careermate.domain.Recruiter;
import com.fpt.careermate.domain.Role;
import com.fpt.careermate.repository.AccountRepo;
import com.fpt.careermate.repository.RecruiterRepo;
import com.fpt.careermate.repository.RoleRepo;
import com.fpt.careermate.services.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.dto.response.NewRecruiterResponse;
import com.fpt.careermate.services.dto.response.RecruiterApprovalResponse;
import com.fpt.careermate.services.impl.RecruiterService;
import com.fpt.careermate.services.mapper.RecruiterMapper;
import com.fpt.careermate.util.UrlValidator;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterImp implements RecruiterService {

    RecruiterRepo recruiterRepo;
    RecruiterMapper recruiterMapper;
    UrlValidator urlValidator;
    AuthenticationImp authenticationImp;
    AccountRepo accountRepo;
    RoleRepo roleRepo;

    // Removed @PreAuthorize - now candidates can create recruiter profiles
    // Flow: User signs up (CANDIDATE role) → Creates recruiter profile → Admin approves → Role changes to RECRUITER
    @Override
    public NewRecruiterResponse createRecruiter(RecruiterCreationRequest request) {
        // Check website
        if(!urlValidator.isWebsiteReachable(request.getWebsite())) throw new AppException(ErrorCode.INVALID_WEBSITE);
        // Check logo URL
        if(!urlValidator.isImageUrlValid(request.getLogoUrl())) throw new AppException(ErrorCode.INVALID_LOGO_URL);

        // Check duplicate recruiter for the account
        recruiterRepo.findByAccount_Id(authenticationImp.findByEmail().getId())
                .ifPresent(recruiter -> {
                    throw new AppException(ErrorCode.RECRUITER_ALREADY_EXISTS);
                });

        Recruiter recruiter = recruiterMapper.toRecruiter(request);
        recruiter.setAccount(authenticationImp.findByEmail());
        recruiter.setRating(0.0f); // Set default rating to avoid null value error

        // save to db, convert to response and return
        return recruiterMapper.toNewRecruiterResponse(recruiterRepo.save(recruiter));
    }

    @Override
    public List<RecruiterApprovalResponse> getPendingRecruiters() {
        // Get all recruiters whose accounts have CANDIDATE role (pending approval)
        Role candidateRole = roleRepo.findById(PredefineRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        return recruiterRepo.findAll().stream()
                .filter(recruiter -> recruiter.getAccount().getRoles().contains(candidateRole)
                        && !hasRecruiterRole(recruiter.getAccount()))
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecruiterApprovalResponse approveRecruiter(int recruiterId) {
        // Find recruiter
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        Account account = recruiter.getAccount();

        // Check if already has RECRUITER role
        if (hasRecruiterRole(account)) {
            throw new AppException(ErrorCode.RECRUITER_ALREADY_APPROVED);
        }

        // Add RECRUITER role
        Role recruiterRole = roleRepo.findById(PredefineRole.RECRUITER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        Set<Role> roles = new HashSet<>(account.getRoles());
        roles.add(recruiterRole);
        account.setRoles(roles);

        accountRepo.save(account);

        log.info("Recruiter profile approved. Account ID: {}, Recruiter ID: {}", account.getId(), recruiterId);

        return mapToApprovalResponse(recruiter);
    }

    @Override
    @Transactional
    public void rejectRecruiter(int recruiterId, String reason) {
        // Find recruiter
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        Account account = recruiter.getAccount();

        // Delete recruiter profile (account remains as CANDIDATE)
        recruiterRepo.delete(recruiter);

        log.info("Recruiter profile rejected and deleted. Account ID: {}, Recruiter ID: {}, Reason: {}",
                account.getId(), recruiterId, reason);
    }

    @Override
    public List<RecruiterApprovalResponse> getAllRecruiters() {
        // Get all recruiters whose accounts have RECRUITER role (approved)
        Role recruiterRole = roleRepo.findById(PredefineRole.RECRUITER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        return recruiterRepo.findAll().stream()
                .filter(recruiter -> recruiter.getAccount().getRoles().contains(recruiterRole))
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private boolean hasRecruiterRole(Account account) {
        return account.getRoles().stream()
                .anyMatch(role -> PredefineRole.RECRUITER_ROLE.equals(role.getName()));
    }

    private RecruiterApprovalResponse mapToApprovalResponse(Recruiter recruiter) {
        RecruiterApprovalResponse response = recruiterMapper.toRecruiterApprovalResponse(recruiter);
        String currentRole = hasRecruiterRole(recruiter.getAccount()) ? "RECRUITER" : "CANDIDATE";
        response.setAccountRole(currentRole);
        return response;
    }

}
