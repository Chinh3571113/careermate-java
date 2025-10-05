package com.fpt.careermate.domain;

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
    String fullName;

    @Column(name = "dob")
    LocalDate dob;

    @Column(name = "gender")
    String gender;

    @Column(name = "phone")
    String phone;

    @Column(name = "address")
    String address;

    @Column(name = "image")
    String image;
}
