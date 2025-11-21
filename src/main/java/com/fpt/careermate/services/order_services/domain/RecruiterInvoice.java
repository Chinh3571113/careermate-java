package com.fpt.careermate.services.order_services.domain;

import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "recruiter_invoice")
public class RecruiterInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    Long amount;
    String status;
    LocalDate startDate;
    LocalDate endDate;
    LocalDate cancelledAt;

    @ManyToOne
    @JoinColumn(name = "recruiter_package_id")
    RecruiterPackage recruiterPackage;

    @OneToOne
    @JoinColumn(name = "recruiter_id")
    Recruiter recruiter;

    boolean isActive;
}
