package com.fpt.careermate.services.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EducationResponse {
    int educationId;
    String school;
    String major;
    String degree;
    LocalDate startDate;
    LocalDate endDate;
}

