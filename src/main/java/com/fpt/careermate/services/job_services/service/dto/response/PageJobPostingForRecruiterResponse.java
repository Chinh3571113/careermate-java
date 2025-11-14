package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageJobPostingForRecruiterResponse {
    List<JobPostingForRecruiterResponse> content;
    int number;          // The current page number (starts from 0)
    int size;            // The number of elements per page (page size)
    long totalElements;  // The total number of elements across all pages
    int totalPages;      // The total number of available pages
    boolean first;       // Indicates whether this is the first page
    boolean last;        // Indicates whether this is the last page
}
