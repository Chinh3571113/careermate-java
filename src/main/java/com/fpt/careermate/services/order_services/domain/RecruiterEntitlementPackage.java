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
@Entity(name = "recruiter_entitlement_package")
public class RecruiterEntitlementPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    boolean enabled;
    int limitValue;
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "recruiter_entitlement_id")
    RecruiterEntitlement recruiterEntitlement;

    @ManyToOne
    @JoinColumn(name = "recruiter_package_id")
    RecruiterPackage recruiterPackage;

    public RecruiterEntitlementPackage(
            boolean enabled,
            int limitValue,
            LocalDateTime createdAt,
            RecruiterEntitlement recruiterEntitlement,
            RecruiterPackage recruiterPackage
    ) {
        this.enabled = enabled;
        this.limitValue = limitValue;
        this.createdAt = createdAt;
        this.recruiterEntitlement = recruiterEntitlement;
        this.recruiterPackage = recruiterPackage;
    }
}
