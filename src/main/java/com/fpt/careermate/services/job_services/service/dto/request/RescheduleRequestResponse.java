package com.fpt.careermate.services.job_services.service.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO for responding to a reschedule request
 * 
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RescheduleRequestResponse {

    @Size(max = 1000, message = "Response notes cannot exceed 1000 characters")
    String responseNotes;
}
