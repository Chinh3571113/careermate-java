package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.dto.request.GeneralInfoRequest;
import com.fpt.careermate.services.dto.response.CandidateProfileResponse;
import com.fpt.careermate.services.dto.response.GeneralInfoResponse;
import com.fpt.careermate.services.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CandidateProfileService {
    PageResponse<CandidateProfileResponse> findAll(Pageable pageable);
    CandidateProfileResponse saveOrUpdateCandidateProfile(CandidateProfileRequest request);
    void deleteProfile(int id);
    GeneralInfoResponse saveCandidateGeneralInfo(GeneralInfoRequest request);
    CandidateProfileResponse getCandidateProfileById();
    GeneralInfoResponse getCandidateGeneralInfoById();
    GeneralInfoResponse updateCandidateGeneralInfo(GeneralInfoRequest request);
}
