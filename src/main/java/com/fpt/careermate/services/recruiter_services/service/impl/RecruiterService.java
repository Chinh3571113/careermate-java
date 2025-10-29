package com.fpt.careermate.services.recruiter_services.service.impl;


import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.recruiter_services.service.dto.response.NewRecruiterResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterApprovalResponse;

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
}
