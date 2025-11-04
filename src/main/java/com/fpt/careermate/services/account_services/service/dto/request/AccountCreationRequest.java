package com.fpt.careermate.services.account_services.service.dto.request;

import com.fpt.careermate.common.validator.Account.PasswordConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCreationRequest {
    @NotNull(message = "Username cannot be null")
    @NotBlank(message = "Username cannot be blank")
    String username;
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @NotNull(message = "Email cannot be null")
    String email;
    @NotBlank(message = "Password cannot be blank")
    @NotNull(message = "Password cannot be null")
    @PasswordConstraint(min = 8, message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    String password;
    String status;


}
