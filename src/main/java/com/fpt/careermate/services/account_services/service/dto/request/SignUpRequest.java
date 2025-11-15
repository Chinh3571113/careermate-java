package com.fpt.careermate.services.account_services.service.dto.request;


import com.fpt.careermate.common.validator.Account.PasswordConstraint;
import com.fpt.careermate.common.validator.CandidateProfile.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignUpRequest {
    @NotBlank(message = "Username cannot be blank")
    String fullName;
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email;
    @NotBlank(message = "Password cannot be blank")
    @PasswordConstraint(message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character", min = 8)
    String password;
    @NotNull(message = "Date of birth is required")
    @DobConstraint(message = "Date of birth must be in the past and you must be at least 18 years old")
    LocalDate dateOfBirth;
}
