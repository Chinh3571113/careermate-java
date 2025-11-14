package com.fpt.careermate.services.recruiter_services.service.impl;


import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterUpdateRequest;
import com.fpt.careermate.services.recruiter_services.service.dto.response.NewRecruiterResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterApprovalResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterProfileResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterUpdateRequestResponse;
import com.fpt.careermate.services.authentication_services.service.dto.request.RecruiterRegistrationRequest;

import java.util.List;

public interface RecruiterService {
    NewRecruiterResponse createRecruiter(RecruiterCreationRequest request);

    // Admin view methods (approval/reject now in RegistrationService)
    List<RecruiterApprovalResponse> getPendingRecruiters();
    List<RecruiterApprovalResponse> getAllRecruiters();

    // New paginated methods with filtering and search
    PageResponse<RecruiterApprovalResponse> getRecruitersByStatus(String status, int page, int size, String sortBy, String sortDir);
    PageResponse<RecruiterApprovalResponse> searchRecruiters(String status, String search, int page, int size, String sortBy, String sortDir);
    RecruiterApprovalResponse getRecruiterById(int recruiterId);

    // Recruiter profile management
    RecruiterProfileResponse getMyProfile();
    RecruiterUpdateRequestResponse requestProfileUpdate(RecruiterUpdateRequest request);
    List<RecruiterUpdateRequestResponse> getMyUpdateRequests();

    // Admin - Profile update request management
    PageResponse<RecruiterUpdateRequestResponse> getAllUpdateRequests(String status, int page, int size, String sortBy, String sortDir);
    PageResponse<RecruiterUpdateRequestResponse> searchUpdateRequests(String status, String search, int page, int size, String sortBy, String sortDir);
    RecruiterUpdateRequestResponse getUpdateRequestById(int requestId);
    void approveUpdateRequest(int requestId, String adminNote);
    void rejectUpdateRequest(int requestId, String rejectionReason);

    // Recruiter self-service methods
    RecruiterApprovalResponse getMyRecruiterProfile();
    void updateOrganizationInfo(RecruiterRegistrationRequest.OrganizationInfo orgInfo);
}
