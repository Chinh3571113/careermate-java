package com.fpt.careermate.services.dto.request;

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
public class CertificateRequest {
    @NotBlank(message = "Certificate name is required")
    String name;
    @NotBlank(message = "Organization is required")
    String organization;
    @NotNull(message = "Date is required")
    LocalDate getDate;
    String certificateUrl;
    String description;
}
