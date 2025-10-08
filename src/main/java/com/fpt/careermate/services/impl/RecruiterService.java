package com.fpt.careermate.services.impl;


import com.fpt.careermate.services.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.dto.response.NewRecruiterResponse;

public interface RecruiterService {
    NewRecruiterResponse createRecruiter(RecruiterCreationRequest request);
}
