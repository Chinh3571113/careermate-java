package com.fpt.careermate.services.order_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "entitlement_package")
public class EntitlementPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    boolean enabled;
    int limitValue;
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "entitlement_id")
    Entitlement entitlement;

    @ManyToOne
    @JoinColumn(name = "package_id")
    CandidatePackage candidatePackage;

    public EntitlementPackage(
            boolean enabled,
            int limitValue,
            LocalDateTime createdAt,
            Entitlement entitlement,
            CandidatePackage candidatePackage
    ) {
        this.enabled = enabled;
        this.limitValue = limitValue;
        this.createdAt = createdAt;
        this.entitlement = entitlement;
        this.candidatePackage = candidatePackage;
    }
}
