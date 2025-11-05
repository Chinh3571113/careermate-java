package com.fpt.careermate.services.recruiter_services.service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterUpdateRequest {

    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    String companyName;

    @URL(message = "Invalid Website URL")
    String website;

    @URL(message = "Invalid Logo URL")
    String logoUrl;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    String about;

    // Organization fields
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

