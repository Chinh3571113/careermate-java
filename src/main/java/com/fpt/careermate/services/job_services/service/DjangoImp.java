package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.util.ApiClient;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DjangoImp {
    String BASE_URL = "http://localhost:8000/api/v1/job-posting/";

    ApiClient apiClient;

    // Add a new job posting to Django to add to weaviate
    public void addJobPosting(JobPosting jobPosting) {
        String url = BASE_URL + "creation";

        // Convert JobDescription -> List<String> skills
        List<String> skills = jobPosting.getJobDescriptions().stream()
                .map(jd -> jd.getJdSkill().getName())
                .toList();

        Map<String, Object> body = Map.of(
                "id", jobPosting.getId(),
                "title", jobPosting.getTitle(),
                "description", jobPosting.getDescription(),
                "address", jobPosting.getAddress(),
                "skills", skills
        );

        // Call Django API
        apiClient.post(url, apiClient.getToken(), body);
    }

}
