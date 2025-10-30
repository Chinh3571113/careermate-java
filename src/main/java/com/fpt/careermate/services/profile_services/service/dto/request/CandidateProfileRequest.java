package com.fpt.careermate.services.profile_services.service.dto.request;

import com.fpt.careermate.common.validator.CandidateProfile.DobConstraint;
import com.fpt.careermate.common.validator.CandidateProfile.PhoneConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateProfileRequest {
    @NotNull(message = "Day of birth is required")
    @DobConstraint(message = "Date of birth must be in the past and you must be at least 18 years old")
    LocalDate dob;
    @NotNull(message = "title is required")
    String title;
    @NotBlank(message = "full name is required")
    String fullName;
    @NotNull(message = "phone number is required")
    @PhoneConstraint(message = "Phone number is not valid")
    String phone;
    @NotNull(message = "address is required")
    String address;
    String image;

    @NotNull(message = "Gender is required")
    String gender;
    String link;

}
