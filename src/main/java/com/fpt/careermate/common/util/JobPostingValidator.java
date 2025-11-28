package com.fpt.careermate.common.util;

import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.repository.JdSkillRepo;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.job_services.service.dto.request.JdSkillRequest;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostingValidator {

    @Autowired
    final JobPostingRepo jobPostingRepo;
    final JdSkillRepo jdSkillRepo;

    /**
     * Check duplicate job posting title within the same recruiter.
     * Different recruiters CAN have job postings with the same title.
     */
    public void checkDuplicateJobPostingTitle(String title, int recruiterId) {
        Optional<JobPosting> existingJobPosting = jobPostingRepo.findByTitleAndRecruiterId(title, recruiterId);
        if (existingJobPosting.isPresent()) {
            throw new AppException(ErrorCode.DUPLICATE_JOB_POSTING_TITLE);
        }
    }

    /**
     * Check duplicate job posting title within the same recruiter, excluding current job posting.
     * Used when updating a job posting.
     */
    public void checkDuplicateJobPostingTitleForUpdate(String title, int recruiterId, int currentJobPostingId) {
        Optional<JobPosting> existingJobPosting = jobPostingRepo.findByTitleAndRecruiterId(title, recruiterId);
        if (existingJobPosting.isPresent() && existingJobPosting.get().getId() != currentJobPostingId) {
            throw new AppException(ErrorCode.DUPLICATE_JOB_POSTING_TITLE);
        }
    }

    public void validateExpirationDate(LocalDate expirationDate) {
        if (expirationDate.isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_EXPIRATION_DATE);
        }
    }

    // Validate existing skill in JdSkill list
    public void validateJdSkill(Set<JdSkillRequest> jdSkillRequests) {
        AtomicBoolean isExisted = new AtomicBoolean(false);

        jdSkillRequests.forEach(jdskillRequest -> {
            jdSkillRepo.findAll().forEach(jdskill -> {
                if (jdskill.getId() == jdskillRequest.getId()) {
                    isExisted.set(true);
                }
            });

            if(!isExisted.get()) {
                throw new AppException(ErrorCode.JD_SKILL_NOT_FOUND);
            }

            isExisted.set(false);
        });


    }

}
