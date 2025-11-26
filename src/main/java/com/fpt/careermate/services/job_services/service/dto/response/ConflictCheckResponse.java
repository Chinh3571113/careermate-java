package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConflictCheckResponse {
    Boolean hasConflict;
    String conflictReason;
    List<ConflictDetail> conflicts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ConflictDetail {
        String conflictType;  // INTERVIEW_OVERLAP, TIME_OFF, OUTSIDE_WORKING_HOURS, MAX_INTERVIEWS_REACHED
        LocalDateTime conflictStart;
        LocalDateTime conflictEnd;
        Integer conflictingInterviewId;
        String description;
    }
}
