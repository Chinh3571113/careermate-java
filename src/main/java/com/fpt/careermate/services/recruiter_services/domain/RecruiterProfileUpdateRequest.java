package com.fpt.careermate.services.recruiter_services.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "recruiter_profile_update_requests")
public class RecruiterProfileUpdateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    @JoinColumn(name = "recruiter_id", nullable = false)
    Recruiter recruiter;

    // New profile information
    @Size(max = 200)
    @Column(name = "new_company_name")
    String newCompanyName;

    @Size(max = 255)
    @Column(name = "new_website")
    String newWebsite;

    @Size(max = 500)
    @Column(name = "new_logo_url")
    String newLogoUrl;

    @Column(name = "new_about", columnDefinition = "TEXT")
    String newAbout;

    @Size(max = 100)
    @Column(name = "new_company_email")
    String newCompanyEmail;

    @Size(max = 100)
    @Column(name = "new_contact_person")
    String newContactPerson;

    @Size(max = 20)
    @Column(name = "new_phone_number")
    String newPhoneNumber;

    @Size(max = 500)
    @Column(name = "new_company_address")
    String newCompanyAddress;

    @Column(name = "status", nullable = false)
    @Builder.Default
    String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "admin_note")
    String adminNote;

    @Column(name = "rejection_reason")
    String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "reviewed_at")
    LocalDateTime reviewedAt;
}

