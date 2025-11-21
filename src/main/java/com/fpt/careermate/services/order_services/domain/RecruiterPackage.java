package com.fpt.careermate.services.order_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "recruiter_package")
public class RecruiterPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String name;
    Long price;
    int durationDays;

    LocalDateTime createAt;

    @OneToMany(mappedBy = "recruiterPackage")
    List<RecruiterEntitlementPackage> recruiterEntitlementPackages;

    int priority;

    @OneToMany(mappedBy = "recruiterPackage")
    List<RecruiterInvoice> recruiterInvoices;
}

