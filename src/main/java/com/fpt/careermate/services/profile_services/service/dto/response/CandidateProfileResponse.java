package com.fpt.careermate.services.profile_services.service.dto.response;

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

public class CandidateProfileResponse {
    int id;
    LocalDate dob;
    String title;
    String fullName;
    String phone;
    String address;
    String image;
    String gender;

    String link;
}
