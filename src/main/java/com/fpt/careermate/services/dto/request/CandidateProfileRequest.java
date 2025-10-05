package com.fpt.careermate.services.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateProfileRequest {
    @NotNull(message = "Day of birth is required")
    LocalDate dob;
    @NotNull(message = "title is required")
    String title;
    @NotNull(message = "phone number is required")
    String phone;
    @NotNull(message = "address is required")
    String address;
    String image;
    String jobLevel;
    Integer experience;
    String gender;
    String link;

}
