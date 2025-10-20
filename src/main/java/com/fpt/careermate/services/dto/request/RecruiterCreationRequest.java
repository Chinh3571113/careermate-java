package com.fpt.careermate.services.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterCreationRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    String companyName;

    @NotBlank(message = "Website is required")
    @URL(message = "Invalid Website URL")
    String website;

    @NotBlank(message = "Logo URL is required")
    @URL(message = "Invalid Logo URL")
    String logoUrl;

    @NotBlank(message = "Company description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    String about;

    // Rating is optional - will default to 0.0 if not provided
    @Min(0)
    @Max(5)
    @Digits(integer = 1, fraction = 2)
    Float rating;

    // ===== ORGANIZATION VERIFICATION FIELDS (Only 4 fields) =====

    @Size(max = 100, message = "Business license number cannot exceed 100 characters")
    String businessLicense; // Business registration number / Tax ID (optional initially, can be added later)

    @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
    String contactPerson; // Contact person name

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
    @Size(max = 20)
    String phoneNumber; // Company phone number

    @Size(max = 500, message = "Company address cannot exceed 500 characters")
    String companyAddress; // Physical address

}
