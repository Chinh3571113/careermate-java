package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.services.job_services.domain.JobPosting;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WeaviateImp {

    WeaviateClient weaviateClient;

    // Thêm job posting vào weaviate để cho job posting recommendation
    public void addJobPostingToWeaviate(JobPosting savedPostgres) {
        // Chuyển List<String> skills sang định dạng String[]
        List<String> skills = savedPostgres.getJobDescriptions().stream()
                .map(jd -> jd.getJdSkill().getName())
                .toList();

        // Tạo job posting map để thêm vào weaviate
        Map<String,Object> jobPostingMap = new HashMap<>();
        jobPostingMap.put("jobId", savedPostgres.getId());
        jobPostingMap.put("title", savedPostgres.getTitle());
        jobPostingMap.put("description", savedPostgres.getDescription());
        jobPostingMap.put("skills", skills);
        jobPostingMap.put("address", savedPostgres.getAddress());

        Result<WeaviateObject> result = weaviateClient.data().creator()
                .withClassName("JobPosting")
                .withProperties(jobPostingMap)
                .run();
    }

}
