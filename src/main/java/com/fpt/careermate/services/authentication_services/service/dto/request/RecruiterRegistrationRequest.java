package com.fpt.careermate.services.authentication_services.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterRegistrationRequest {

    // Account information
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;

    // Organization information (embedded)
    @NotNull(message = "Organization info is required")
    @Valid
    OrganizationInfo organizationInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrganizationInfo {

        @NotBlank(message = "Company name is required")
        @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
        String companyName;

        @NotBlank(message = "Website is required")
        @URL(message = "Invalid Website URL")
        String website;

        @URL(message = "Invalid Logo URL")
        String logoUrl; // Optional

        @NotBlank(message = "Company description is required")
        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String about;

        @Email(message = "Invalid company email format")
        @Size(max = 100, message = "Company email cannot exceed 100 characters")
        String companyEmail;

        @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
        String contactPerson;

        @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
        @Size(max = 20)
        String phoneNumber;

        @Size(max = 500, message = "Company address cannot exceed 500 characters")
        String companyAddress;
    }
}

