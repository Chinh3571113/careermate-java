package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for dispute list item in admin dashboard.
 * Contains summary information without full evidence details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeListResponse {

    private Long disputeId;
    private Long jobApplyId;
    private String status;

    // Candidate information
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidateClaimedStatus;
    private String candidateClaimedTerminationType;

    // Recruiter information
    private Long recruiterId;
    private String recruiterName;
    private String recruiterEmail;
    private String recruiterClaimedStatus;
    private String recruiterClaimedTerminationType;

    // Company information
    private String companyName;
    private String positionTitle;

    // Dispute metadata
    private LocalDateTime createdAt;
    private int daysSinceCreated;
    private boolean isHighPriority;
    private int candidateEvidenceCount;
    private int recruiterEvidenceCount;

    // Quick stats
    private String conflictSummary;
    private String urgencyLevel;
}
