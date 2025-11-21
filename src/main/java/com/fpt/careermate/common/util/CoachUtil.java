package com.fpt.careermate.common.util;

import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CoachUtil {

    AuthenticationImp authenticationImp;
    CandidateRepo candidateRepo;
    RecruiterRepo recruiterRepo;

    public Candidate getCurrentCandidate() {
        Account account = authenticationImp.findByEmail();
        Optional<Candidate> exsting = candidateRepo.findByAccount_Id(account.getId());
        return exsting.get();
    }

    public Recruiter getCurrentRecruiter() {
        Account account = authenticationImp.findByEmail();
        Optional<Recruiter> exsting = recruiterRepo.findByAccount_Id(account.getId());
        return exsting.get();
    }

}
