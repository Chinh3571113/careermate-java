package com.fpt.careermate.services.order_services.domain;

import com.fpt.careermate.services.profile_services.domain.Candidate;
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
@Entity(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    Long amount;
    String status;
    LocalDate startDate;
    LocalDate endDate;
    LocalDate createAt;
    LocalDate cancelledAt;

    @ManyToOne
    @JoinColumn(name = "package_id")
    CandidatePackage candidatePackage;

    @OneToOne
    @JoinColumn(name = "candidate_id")
    Candidate candidate;

    boolean isActive;
}
