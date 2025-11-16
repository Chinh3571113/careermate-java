package com.fpt.careermate.services.profile_services.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class BaseUser {

    @Column(name = "full_name")
    String fullName = "";

    @Column(name = "dob")
    LocalDate dob;

    @Column(name = "gender")
    @Builder.Default
    String gender = "";

    @Column(name = "phone")
    @Builder.Default
    String phone = "";

    @Column(name = "address")
    @Builder.Default
    String address = "";

    @Column(name = "image")
    @Builder.Default
    String image = "";
}
