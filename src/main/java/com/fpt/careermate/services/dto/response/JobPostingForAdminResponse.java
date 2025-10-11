package com.fpt.careermate.services.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostingForAdminResponse {

    int id;
    String title;
    String address;
    String status;
    String recruiterName;
    LocalDate expirationDate;
    LocalDate createAt;

}
