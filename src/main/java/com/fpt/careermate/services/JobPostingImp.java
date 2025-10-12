package com.fpt.careermate.services;

import com.fpt.careermate.constant.StatusJobPosting;
import com.fpt.careermate.domain.*;
import com.fpt.careermate.repository.JdSkillRepo;
import com.fpt.careermate.repository.JobDescriptionRepo;
import com.fpt.careermate.repository.JobPostingRepo;
import com.fpt.careermate.repository.RecruiterRepo;
import com.fpt.careermate.services.dto.request.JdSkillRequest;
import com.fpt.careermate.services.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.dto.response.JobPostingForRecruiterResponse;
import com.fpt.careermate.services.impl.JobPostingService;
import com.fpt.careermate.services.mapper.JobPostingMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobPostingImp implements JobPostingService {

    JobPostingRepo jobPostingRepo;
    RecruiterRepo recruiterRepo;
    JdSkillRepo jdSkillRepo;
    JobDescriptionRepo jobDescriptionRepo;
    JobPostingMapper jobPostingMapper;
    AuthenticationImp authenticationImp;

    // Recruiter create job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void createJobPosting(JobPostingCreationRequest request) {
        // Check expiration date
        if (!request.getExpirationDate().isAfter(LocalDate.now())) throw new AppException(ErrorCode.INVALID_EXPIRATION_DATE);
        // Check duplicate title
        jobPostingRepo.findByTitle(request.getTitle())
                .ifPresent(jobPosting -> {
                    throw new AppException(ErrorCode.DUPLICATE_JOB_POSTING_TITLE);
                });

        Recruiter recruiter = getMyRecruiter();

        JobPosting jobPosting = jobPostingMapper.toJobPosting(request);
        jobPosting.setCreateAt(LocalDate.now());
        jobPosting.setRecruiter(recruiter);
        jobPosting.setStatus(StatusJobPosting.PENDING);

        jobPostingRepo.save(jobPosting);

        Set<JobDescription> jobDescriptions = new HashSet<>();
        for(JdSkillRequest skillReq : request.getJdSkills()) {
//            a) find jdSkill by id
            Optional<JdSkill> exstingJdSkill = jdSkillRepo.findById(skillReq.getId());
            JdSkill jdSkill = exstingJdSkill.get();

//            b) Create JobDescription link
            JobDescription jd = new JobDescription();
            jd.setJobPosting(jobPosting);
            jd.setJdSkill(jdSkill);
            jd.setMustToHave(skillReq.isMustToHave());

            jobDescriptions.add(jd);
        }

//        Save all JobDescription
        jobDescriptionRepo.saveAll(jobDescriptions);

        jobPosting.setJobDescriptions(jobDescriptions);

        jobPostingRepo.save(jobPosting);
    }

    // Get all job postings of the current recruiter with all status
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public List<JobPostingForRecruiterResponse> getAllJobPostingForRecruiter() {
        Recruiter recruiter = getMyRecruiter();

        return jobPostingMapper.
                toJobPostingForRecruiterResponseList(jobPostingRepo.findAllByRecruiter_Id(recruiter.getId()));
    }

    // Get current recruiter
    private Recruiter getMyRecruiter(){
        Account currentAccount = authenticationImp.findByEmail();
        Optional<Recruiter> currentRecruiter = recruiterRepo.findByAccount_Id(currentAccount.getId());
        return currentRecruiter.get();
    }

}
