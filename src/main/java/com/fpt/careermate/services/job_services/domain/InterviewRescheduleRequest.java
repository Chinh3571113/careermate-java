package com.fpt.careermate.services.job_services.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a reschedule request for an interview
 * Supports workflows where either party can request reschedules
 * Includes consent tracking when required
 *
 * @since 1.0
 */
@Entity(name = "interview_reschedule_request")
@Table(indexes = {
        @Index(name = "idx_reschedule_status", columnList = "status"),
        @Index(name = "idx_reschedule_interview", columnList = "interview_schedule_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewRescheduleRequest {

    /**
     * Who initiated the reschedule request
     */
    public enum RequestedBy {
        RECRUITER,
        CANDIDATE
    }

    /**
     * Current status of the reschedule request
     */
    public enum RescheduleStatus {
        /**
         * Waiting for the other party to consent
         */
        PENDING_CONSENT,
        
        /**
         * Request accepted and interview rescheduled
         */
        ACCEPTED,
        
        /**
         * Request rejected by the other party
         */
        REJECTED,
        
        /**
         * Request expired without response
         */
        EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reschedule_request_id")
    private Long id;

    /**
     * The interview being rescheduled
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_schedule_id", nullable = false)
    private InterviewSchedule interviewSchedule;

    /**
     * Original scheduled date/time
     */
    @Column(name = "original_date", nullable = false)
    private LocalDateTime originalDate;

    /**
     * Requested new date/time
     */
    @Column(name = "new_requested_date", nullable = false)
    private LocalDateTime newRequestedDate;

    /**
     * Reason for rescheduling
     */
    @Column(name = "reason", length = 1000)
    private String reason;

    /**
     * Who requested the reschedule
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "requested_by", nullable = false, length = 20)
    private RequestedBy requestedBy;

    /**
     * Whether consent is required from the other party
     */
    @Column(name = "requires_consent", nullable = false)
    private Boolean requiresConsent;

    /**
     * Whether consent was given
     */
    @Column(name = "consent_given")
    private Boolean consentGiven;

    /**
     * When consent was given
     */
    @Column(name = "consent_given_at")
    private LocalDateTime consentGivenAt;

    /**
     * Current status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RescheduleStatus status;

    /**
     * Response from the other party
     */
    @Column(name = "response_notes", length = 1000)
    private String responseNotes;

    /**
     * When the request expires if no response
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Check if request has expired
     */
    public boolean hasExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if request is still pending
     */
    public boolean isPending() {
        return status == RescheduleStatus.PENDING_CONSENT && !hasExpired();
    }

    /**
     * Check if enough time until original interview (>= 24 hours)
     */
    public boolean hasEnoughNotice() {
        return LocalDateTime.now().plusHours(24).isBefore(originalDate);
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = RescheduleStatus.PENDING_CONSENT;
        }
        if (requiresConsent == null) {
            requiresConsent = true;
        }
        if (consentGiven == null) {
            consentGiven = false;
        }
        // Set expiration to 24 hours from creation if not set
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusHours(24);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Auto-update status if expired
        if (hasExpired() && status == RescheduleStatus.PENDING_CONSENT) {
            status = RescheduleStatus.EXPIRED;
        }
    }
}
