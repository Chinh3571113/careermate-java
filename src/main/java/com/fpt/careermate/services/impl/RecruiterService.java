package com.fpt.careermate.services.impl;


import com.fpt.careermate.services.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.dto.response.NewRecruiterResponse;
import com.fpt.careermate.services.dto.response.RecruiterApprovalResponse;

import java.util.List;

public interface RecruiterService {
    NewRecruiterResponse createRecruiter(RecruiterCreationRequest request);

    // Admin approval methods
    List<RecruiterApprovalResponse> getPendingRecruiters();
    RecruiterApprovalResponse approveRecruiter(int recruiterId);
    void rejectRecruiter(int recruiterId, String reason);
    List<RecruiterApprovalResponse> getAllRecruiters();
}
