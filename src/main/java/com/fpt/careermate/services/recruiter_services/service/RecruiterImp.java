package com.fpt.careermate.services.recruiter_services.service;

import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.recruiter_services.service.dto.response.NewRecruiterResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterApprovalResponse;
import com.fpt.careermate.services.recruiter_services.service.impl.RecruiterService;
import com.fpt.careermate.services.recruiter_services.service.mapper.RecruiterMapper;
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

import java.util.List;
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

    // Method for authenticated users to add their recruiter/company profile
    // Used by existing accounts that want to add organization information
    @Override
    public NewRecruiterResponse createRecruiter(RecruiterCreationRequest request) {
        // Check website
        if(!urlValidator.isWebsiteReachable(request.getWebsite())) throw new AppException(ErrorCode.INVALID_WEBSITE);

        // Check logo URL only if provided (optional field)
        if(request.getLogoUrl() != null && !request.getLogoUrl().isEmpty()) {
            if(!urlValidator.isImageUrlValid(request.getLogoUrl())) {
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
        if(recruiter.getLogoUrl() == null || recruiter.getLogoUrl().isEmpty()) {
            recruiter.setLogoUrl("https://via.placeholder.com/150");
        }

        // save to db, convert to response and return
        return recruiterMapper.toNewRecruiterResponse(recruiterRepo.save(recruiter));
    }

    @Override
    public List<RecruiterApprovalResponse> getPendingRecruiters() {
        // Get all recruiters with PENDING status (waiting for admin approval)
        return recruiterRepo.findAll().stream()
                .filter(recruiter -> "PENDING".equals(recruiter.getAccount().getStatus()))
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecruiterApprovalResponse> getAllRecruiters() {
        // Get all recruiters with ACTIVE status (approved by admin)
        return recruiterRepo.findAll().stream()
                .filter(recruiter -> "ACTIVE".equals(recruiter.getAccount().getStatus()))
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<RecruiterApprovalResponse> getRecruitersByStatus(String status, int page, int size, String sortBy, String sortDir) {
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
                recruiterPage.getTotalPages()
        );
    }

    @Override
    public PageResponse<RecruiterApprovalResponse> searchRecruiters(String status, String search, int page, int size, String sortBy, String sortDir) {
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
                recruiterPage.getTotalPages()
        );
    }

    @Override
    public RecruiterApprovalResponse getRecruiterById(int recruiterId) {
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));
        return mapToApprovalResponse(recruiter);
    }

    // Helper methods
    private RecruiterApprovalResponse mapToApprovalResponse(Recruiter recruiter) {
        RecruiterApprovalResponse response = recruiterMapper.toRecruiterApprovalResponse(recruiter);
        // Set account status (PENDING, ACTIVE, or BANNED)
        response.setAccountStatus(recruiter.getAccount().getStatus());
        // Role is always RECRUITER for recruiter accounts
        response.setAccountRole("RECRUITER");
        return response;
    }

}
