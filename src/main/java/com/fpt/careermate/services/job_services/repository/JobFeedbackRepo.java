package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.JobFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobFeedbackRepo extends JpaRepository<JobFeedback, Integer> {
    Optional<JobFeedback> findByJobPostingIdAndCandidateCandidateIdAndFeedbackType(
            Integer jobPostingId, Integer candidateId, String feedbackType);

    List<JobFeedback> findByJobPostingId(Integer jobPostingId);

    List<JobFeedback> findByCandidateCandidateId(Integer candidateId);

    List<JobFeedback> findByCandidateCandidateIdAndFeedbackType(Integer candidateId, String feedbackType);
}
