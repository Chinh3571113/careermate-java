package com.fpt.careermate.services.profile_services.service.impl;

import com.fpt.careermate.services.profile_services.service.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.profile_services.service.dto.request.GeneralInfoRequest;
import com.fpt.careermate.services.profile_services.service.dto.response.CandidateProfileResponse;
import com.fpt.careermate.services.profile_services.service.dto.response.GeneralInfoResponse;
import com.fpt.careermate.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CandidateProfileService {
    PageResponse<CandidateProfileResponse> findAll(Pageable pageable);
    CandidateProfileResponse updateCandidateProfile(CandidateProfileRequest request);
    void deleteProfile(int id);
    GeneralInfoResponse saveCandidateGeneralInfo(GeneralInfoRequest request);
    CandidateProfileResponse getCandidateProfile();
    GeneralInfoResponse getCandidateGeneralInfoById();
    GeneralInfoResponse updateCandidateGeneralInfo(GeneralInfoRequest request);
    CandidateProfileResponse saveCandidateProfile(CandidateProfileRequest request);
    CandidateProfileResponse getCandidateProfileById(int id);

}
