package com.fpt.careermate.services;

import com.fpt.careermate.domain.Recruiter;
import com.fpt.careermate.repository.RecruiterRepo;
import com.fpt.careermate.services.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.dto.response.NewRecruiterResponse;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterImp implements RecruiterService {

    RecruiterRepo recruiterRepo;
    RecruiterMapper recruiterMapper;
    UrlValidator urlValidator;
    AuthenticationImp authenticationImp;

    public NewRecruiterResponse createRecruiter(RecruiterCreationRequest request) {
        // Check website
        if(!urlValidator.isWebsiteReachable(request.getWebsite())) throw new AppException(ErrorCode.INVALID_WEBSITE);
        // Check logo URL
        if(!urlValidator.isImageUrlValid(request.getLogoUrl())) throw new AppException(ErrorCode.INVALID_LOGO_URL);

        Recruiter recruiter = recruiterMapper.toRecruiter(request);
        recruiter.setAccount(authenticationImp.findByEmail());

        // save to db, convert to response and return
        return recruiterMapper.toNewRecruiterResponse(recruiterRepo.save(recruiter));
    }

}
