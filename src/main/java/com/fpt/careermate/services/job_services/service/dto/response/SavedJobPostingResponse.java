package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SavedJobPostingResponse {
    int savedJobId;
    String title;
    String companyName;
    String companyAddress;
    String salaryRange;
    String skills;
    String yearOfExperience;
    String workModel;
    String expirationDate;
    String savedAt;
    int jobId;
}
