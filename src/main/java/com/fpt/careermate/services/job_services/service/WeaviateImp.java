package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WeaviateImp {

    // Constants to avoid duplicated literals
    private static final String TEXT2VEC_MODULE = "text2vec-palm";
    private static final String VECTORIZE_PROPERTY_NAME = "vectorizePropertyName";

    WeaviateClient weaviateClient;
    JobPostingRepo jobPostingRepo;

    // Thêm job posting vào weaviate để cho job posting recommendation
    public void addJobPostingToWeaviate(JobPosting savedPostgres) {
        // Re-fetch with jobDescriptions + jdSkill eagerly to avoid lazy loading issues
        JobPosting source = jobPostingRepo.fetchByIdWithSkills(savedPostgres.getId())
                .orElse(savedPostgres);

        List<String> skills;
        try {
            if (source.getJobDescriptions() != null && !source.getJobDescriptions().isEmpty()) {
                skills = source.getJobDescriptions().stream()
                        .filter(jd -> jd.getJdSkill() != null)
                        .map(jd -> jd.getJdSkill().getName())
                        .distinct()
                        .collect(Collectors.toList());
            } else {
                skills = List.of();
            }
        } catch (Exception e) {
            log.warn("Could not load job descriptions for job '{}', using empty skills list", source.getTitle());
            skills = List.of();
        }

        Map<String,Object> jobPostingMap = new HashMap<>();
        jobPostingMap.put("jobId", source.getId());
        jobPostingMap.put("title", source.getTitle());
        jobPostingMap.put("description", source.getDescription());
        jobPostingMap.put("skills", skills);
        jobPostingMap.put("address", source.getAddress());

        weaviateClient.data().creator()
                .withClassName("JobPosting")
                .withProperties(jobPostingMap)
                .run();

        log.info("Indexed job posting '{}' (id={}) to Weaviate with {} skills", source.getTitle(), source.getId(), skills.size());
    }

    // Kiểm tra xem job posting đã tồn tại trong Weaviate chưa
    public boolean isJobPostingExistsInWeaviate(Integer jobId) {
        try {
            String query = String.format(
                "{Get{JobPosting(where:{path:[\"jobId\"],operator:Equal,valueInt:%d}){jobId}}}",
                jobId
            );

            var result = weaviateClient.graphQL().raw()
                    .withQuery(query)
                    .run();

            if (result.hasErrors()) {
                log.warn("Error checking job posting existence in Weaviate: {}",
                        result.getError().getMessages());
                return false;
            }

            var data = result.getResult();
            if (data != null && data.getData() != null) {
                var getData = (Map<String, Object>) data.getData();
                var get = (Map<String, Object>) getData.get("Get");
                if (get != null) {
                    var jobPostings = (List<?>) get.get("JobPosting");
                    return jobPostings != null && !jobPostings.isEmpty();
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Exception while checking job posting existence in Weaviate: {}", e.getMessage());
            return false;
        }
    }

    // Xóa job posting khỏi Weaviate
    public void deleteJobPosting(Integer jobId) {
        try {
            // Query to get the object UUID by jobId
            String query = String.format(
                "{Get{JobPosting(where:{path:[\"jobId\"],operator:Equal,valueInt:%d}){_additional{id}}}}",
                jobId
            );

            var result = weaviateClient.graphQL().raw()
                    .withQuery(query)
                    .run();

            if (result.hasErrors()) {
                log.warn("Error querying job posting for deletion in Weaviate: {}",
                        result.getError().getMessages());
                return;
            }

            var data = result.getResult();
            if (data != null && data.getData() != null) {
                var getData = (Map<String, Object>) data.getData();
                var get = (Map<String, Object>) getData.get("Get");
                if (get != null) {
                    var jobPostings = (List<Map<String, Object>>) get.get("JobPosting");
                    if (jobPostings != null && !jobPostings.isEmpty()) {
                        for (Map<String, Object> jobPosting : jobPostings) {
                            var additional = (Map<String, Object>) jobPosting.get("_additional");
                            if (additional != null) {
                                String uuid = (String) additional.get("id");
                                if (uuid != null) {
                                    // Delete the object by UUID
                                    var deleteResult = weaviateClient.data().deleter()
                                            .withClassName("JobPosting")
                                            .withID(uuid)
                                            .run();

                                    if (deleteResult.hasErrors()) {
                                        log.error("Error deleting job posting from Weaviate: {}",
                                                deleteResult.getError().getMessages());
                                    } else {
                                        log.info("Successfully deleted job posting with jobId={} from Weaviate", jobId);
                                    }
                                }
                            }
                        }
                    } else {
                        log.info("Job posting with jobId={} not found in Weaviate", jobId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception while deleting job posting from Weaviate: {}", e.getMessage(), e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void resetJobPostingCollection() {
        String collectionName = "JobPosting";
        try {
            Result<Boolean> deleteResult = weaviateClient.schema().classDeleter()
                    .withClassName(collectionName)
                    .run();
            if (deleteResult.hasErrors()) {
                log.warn("Could not delete existing collection: {}", deleteResult.getError().getMessages());
            }
            deleteJobPostingInPostgres();
            Thread.sleep(2000);
            createJobPostingCollection(collectionName);
            createJobPostingProperty(collectionName);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        } catch (Exception e) {
            log.error("Error resetting Job Posting collection: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private void createJobPostingCollection(String collectionName) {
        Map<String, Object> moduleConfig = new HashMap<>();
        Map<String, Object> text2vecGoogleAistudio = new HashMap<>();
        text2vecGoogleAistudio.put("vectorizeClassName", false);
        text2vecGoogleAistudio.put("model", "gemini-embedding-001");
        moduleConfig.put(TEXT2VEC_MODULE, text2vecGoogleAistudio);
        WeaviateClass weaviateClass =
                WeaviateClass.builder()
                        .className(collectionName)
                        .description("A collection of job posting.")
                        .vectorizer(TEXT2VEC_MODULE)
                        .moduleConfig(moduleConfig)
                        .build();
        Result<Boolean> result = weaviateClient.schema().classCreator()
                .withClass(weaviateClass)
                .run();
        if (result.hasErrors()) {
            log.error("Error creating Job Posting collection: {}", result.getError().getMessages());
            throw new AppException(ErrorCode.CANNOT_CREATE_JOB_POSTING_COLLECTION);
        }
    }

    private void createJobPostingProperty(String collectionName) {
        // jobId: int
        Property jobIdProp = Property.builder()
                .name("jobId")
                .dataType(Collections.singletonList("int"))
                .description("The id of the job posting")
                .build();
        Result<Boolean> r1 = weaviateClient.schema().propertyCreator()
                .withClassName(collectionName)
                .withProperty(jobIdProp)
                .run();
        if (r1.hasErrors()) {
            throw new AppException(ErrorCode.CANNOT_CREATE_JOB_POSTING_PROPERTY);
        }

        // title: text
        Map<String, Object> titleModuleConfig = new HashMap<>();
        Map<String, Object> titleInner = new HashMap<>();
        titleInner.put("skip", false);
        titleInner.put(VECTORIZE_PROPERTY_NAME, false);
        titleModuleConfig.put(TEXT2VEC_MODULE, titleInner);
        Property titleProp = Property.builder()
                .name("title")
                .dataType(Collections.singletonList("text"))
                .description("The title of the job posting")
                .moduleConfig(titleModuleConfig)
                .build();
        Result<Boolean> r2 = weaviateClient.schema().propertyCreator()
                .withClassName(collectionName)
                .withProperty(titleProp)
                .run();
        if (r2.hasErrors()) {
            throw new AppException(ErrorCode.CANNOT_CREATE_JOB_POSTING_PROPERTY);
        }

        // description: text
        Map<String, Object> descModuleConfig = new HashMap<>();
        Map<String, Object> descInner = new HashMap<>();
        descInner.put("skip", false);
        descInner.put(VECTORIZE_PROPERTY_NAME, false);
        descModuleConfig.put(TEXT2VEC_MODULE, descInner);
        Property descProp = Property.builder()
                .name("description")
                .dataType(Collections.singletonList("text"))
                .description("The description of the job posting")
                .moduleConfig(descModuleConfig)
                .build();
        Result<Boolean> r3 = weaviateClient.schema().propertyCreator()
                .withClassName(collectionName)
                .withProperty(descProp)
                .run();
        if (r3.hasErrors()) {
            throw new AppException(ErrorCode.CANNOT_CREATE_JOB_POSTING_PROPERTY);
        }

        // skills: text[]
        Map<String, Object> skillsModuleConfig = new HashMap<>();
        Map<String, Object> skillsInner = new HashMap<>();
        skillsInner.put("skip", false);
        skillsInner.put(VECTORIZE_PROPERTY_NAME, false);
        skillsModuleConfig.put(TEXT2VEC_MODULE, skillsInner);
        Property skillsProp = Property.builder()
                .name("skills")
                .dataType(Collections.singletonList("text[]"))
                .description("List of skills for the job posting")
                .moduleConfig(skillsModuleConfig)
                .build();
        Result<Boolean> r4 = weaviateClient.schema().propertyCreator()
                .withClassName(collectionName)
                .withProperty(skillsProp)
                .run();
        if (r4.hasErrors()) {
            throw new AppException(ErrorCode.CANNOT_CREATE_JOB_POSTING_PROPERTY);
        }

        // address: text
        Map<String, Object> addrModuleConfig = new HashMap<>();
        Map<String, Object> addrInner = new HashMap<>();
        addrInner.put("skip", false);
        addrInner.put(VECTORIZE_PROPERTY_NAME, false);
        addrModuleConfig.put(TEXT2VEC_MODULE, addrInner);
        Property addrProp = Property.builder()
                .name("address")
                .dataType(Collections.singletonList("text"))
                .description("The address of the job posting")
                .moduleConfig(addrModuleConfig)
                .build();
        Result<Boolean> r5 = weaviateClient.schema().propertyCreator()
                .withClassName(collectionName)
                .withProperty(addrProp)
                .run();
        if (r5.hasErrors()) {
            throw new AppException(ErrorCode.CANNOT_CREATE_JOB_POSTING_PROPERTY);
        }
    }

    private void deleteJobPostingInPostgres(){
        jobPostingRepo.deleteAll();
    }
}
