package com.fpt.careermate.services.order_services.domain;

import com.fpt.careermate.services.profile_services.domain.Candidate;
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
@Entity(name = "entitlement")
public class Entitlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String name;
    String code;
    String unit;
    boolean hasLimit;

    @OneToMany(mappedBy = "entitlement")
    List<EntitlementPackage> entitlementPackages;
}
