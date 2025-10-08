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

    @NotBlank
    @Size(min = 2, max = 100)
    String fullName;

    @NotBlank
    @Size(min = 2, max = 100)
    String companyName;

    @NotBlank
    @URL(message = "Invalid Website")
    String website;

    @NotBlank
    @URL(message = "Invalid Logo URL")
    String logoUrl;

    @Size(max = 2000)
    String about;

    @Min(0)
    @Max(5)
    @Digits(integer = 1, fraction = 2)
    Float rating;

}
