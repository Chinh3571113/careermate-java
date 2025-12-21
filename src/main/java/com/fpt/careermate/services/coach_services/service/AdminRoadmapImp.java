package com.fpt.careermate.services.coach_services.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.coach_services.domain.*;
import com.fpt.careermate.services.coach_services.repository.*;
import com.fpt.careermate.services.coach_services.service.dto.response.*;
import com.fpt.careermate.services.coach_services.service.impl.AdminRoadmapService;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminRoadmapImp implements AdminRoadmapService {

    WeaviateClient client;
    RoadmapRepo roadmapRepo;

    static String roadmapCollection = "Roadmap";
    static String roadmapCollection2 = "Roadmap2";

    @PreAuthorize("hasRole('ADMIN')")
    public void createRoadmapCollection(){
        try {
            // Check if Roadmap class exists
            Result<Boolean> exists = client.schema().exists()
                    .withClassName(roadmapCollection)
                    .run();

            if (exists.getResult()) {
                log.info("Roadmap collection already exists");
                return;
            }

            // Configure text2vec-huggingface with sentence-transformers/all-MiniLM-L6-v2
            Map<String, Object> moduleConfig = new HashMap<>();
            Map<String, Object> text2vecHuggingface = new HashMap<>();
            text2vecHuggingface.put("vectorizeClassName", false);
            text2vecHuggingface.put("model", "sentence-transformers/all-MiniLM-L6-v2");
            moduleConfig.put("text2vec-huggingface", text2vecHuggingface);

            // Vectorized properties configuration
            Map<String, Object> vectorizedConfig = new HashMap<>();
            Map<String, Object> vectorizedText2vec = new HashMap<>();
            vectorizedText2vec.put("skip", false);
            vectorizedText2vec.put("vectorizePropertyName", false);
            vectorizedConfig.put("text2vec-huggingface", vectorizedText2vec);

            // Build Weaviate class for Roadmap
            WeaviateClass weaviateClass = WeaviateClass
                    .builder()
                    .className(roadmapCollection)
                    .description("Roadmap collection for recommendation")
                    .vectorizer("text2vec-huggingface")
                    .moduleConfig(moduleConfig)
                    .properties(Arrays.asList(
                            // Name property (non-vectorized, used as identifier)
                            Property.builder()
                                    .name("name")
                                    .dataType(Arrays.asList("text"))
                                    .description("Roadmap name")
                                    .moduleConfig(vectorizedConfig)
                                    .build()
                    ))
                    .build();

            // Create the schema
            Result<Boolean> createResult = client.schema().classCreator()
                    .withClass(weaviateClass)
                    .run();

            if (createResult.hasErrors()) {
                log.error("Failed to create Roadmap schema: {}", createResult.getError().getMessages());
                throw new AppException(ErrorCode.WEAVIATE_ERROR);
            } else {
                log.info("Roadmap collection created successfully");

                // Populate the collection with existing roadmaps
                populateRoadmapCollection();
            }
        } catch (Exception e) {
            log.error("Error creating Roadmap collection: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.WEAVIATE_ERROR);
        }
    }

    private void populateRoadmapCollection() {
        try {
            // Get all roadmaps from database
            List<Roadmap> roadmaps = roadmapRepo.findAll();

            for (Roadmap roadmap : roadmaps) {
                // Create data object for Weaviate
                Map<String, Object> dataObject = new HashMap<>();
                dataObject.put("name", roadmap.getName());

                // Insert into Weaviate
                Result<WeaviateObject> result =
                        client.data().creator()
                                .withClassName(roadmapCollection)
                                .withProperties(dataObject)
                                .run();

                if (result.hasErrors()) {
                    log.error("Failed to insert roadmap '{}': {}",
                            roadmap.getName(),
                            result.getError().getMessages());
                } else {
                    log.info("Inserted roadmap: {}", roadmap.getName());
                }
            }

            log.info("Roadmap collection populated successfully");
        } catch (Exception e) {
            log.error("Error populating Roadmap collection: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.WEAVIATE_ERROR);
        }
    }

    // Tạo roadmap collection có properties là name và skills (được lấy từ subtopic gộp thành String)
    @PreAuthorize("hasRole('ADMIN')")
    public void createRoadmapCollection2(){
        try {
            // Check if Roadmap class exists
            Result<Boolean> exists = client.schema().exists()
                    .withClassName(roadmapCollection2)
                    .run();

            if (exists.getResult()) {
                log.info("Roadmap collection 2 already exists");
                return;
            }

            // Configure text2vec-huggingface with sentence-transformers/all-MiniLM-L6-v2
            Map<String, Object> moduleConfig = new HashMap<>();
            Map<String, Object> text2vecHuggingface = new HashMap<>();
            text2vecHuggingface.put("vectorizeClassName", false);
            text2vecHuggingface.put("model", "sentence-transformers/all-MiniLM-L6-v2");
            moduleConfig.put("text2vec-huggingface", text2vecHuggingface);

            // Vectorized properties configuration
            Map<String, Object> vectorizedConfig = new HashMap<>();
            Map<String, Object> vectorizedText2vec = new HashMap<>();
            vectorizedText2vec.put("skip", false);
            vectorizedText2vec.put("vectorizePropertyName", false);
            vectorizedConfig.put("text2vec-huggingface", vectorizedText2vec);

            // Non-vectorized properties configuration
            Map<String, Object> skipConfig = new HashMap<>();
            Map<String, Object> skipText2vec = new HashMap<>();
            skipText2vec.put("skip", true);
            skipText2vec.put("vectorizePropertyName", false);
            skipConfig.put("text2vec-huggingface", skipText2vec);

            // Build Weaviate class for Roadmap
            WeaviateClass weaviateClass = WeaviateClass
                    .builder()
                    .className(roadmapCollection2)
                    .description("Roadmap collection 2 for semantic search by role/skills")
                    .vectorizer("text2vec-huggingface")
                    .moduleConfig(moduleConfig)
                    .properties(Arrays.asList(
                            // Name property (non-vectorized, used as identifier)
                            Property.builder()
                                    .name("name")
                                    .dataType(Arrays.asList("text"))
                                    .description("Roadmap name")
                                    .moduleConfig(skipConfig)
                                    .build(),
                            // Skills property (vectorized for semantic matching)
                            Property.builder()
                                    .name("skills")
                                    .dataType(Arrays.asList("text[]"))
                                    .description("Array of skills from all subtopics - vectorized for semantic search")
                                    .moduleConfig(vectorizedConfig)
                                    .build()
                    ))
                    .build();

            // Create the schema
            Result<Boolean> createResult = client.schema().classCreator()
                    .withClass(weaviateClass)
                    .run();

            if (createResult.hasErrors()) {
                log.error("Failed to create Roadmap schema: {}", createResult.getError().getMessages());
                throw new AppException(ErrorCode.WEAVIATE_ERROR);
            } else {
                log.info("Roadmap collection 2 created successfully");

                // Populate the collection with existing roadmaps
                populateRoadmapCollection2();
            }
        } catch (Exception e) {
            log.error("Error creating Roadmap collection: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.WEAVIATE_ERROR);
        }
    }

    private void populateRoadmapCollection2() {
        try {
            // Get all roadmaps from database
            List<Roadmap> roadmaps = roadmapRepo.findAll();

            for (Roadmap roadmap : roadmaps) {
                // Collect all subtopic names (skills) from the roadmap
                List<String> skillsList = new ArrayList<>();

                for (Topic topic : roadmap.getTopics()) {
                    for (Subtopic subtopic : topic.getSubtopics()) {
                        skillsList.add(subtopic.getName());
                    }
                }

                // Create data object for Weaviate
                Map<String, Object> dataObject = new HashMap<>();
                dataObject.put("name", roadmap.getName());
                dataObject.put("skills", skillsList);

                // Insert into Weaviate
                Result<WeaviateObject> result =
                        client.data().creator()
                        .withClassName(roadmapCollection2)
                        .withProperties(dataObject)
                        .run();

                if (result.hasErrors()) {
                    log.error("Failed to insert roadmap '{}': {}",
                            roadmap.getName(),
                            result.getError().getMessages());
                } else {
                    log.info("Inserted roadmap: {}", roadmap.getName());
                }
            }

            log.info("Roadmap collection 2 populated successfully");
        } catch (Exception e) {
            log.error("Error populating Roadmap collection 2: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.WEAVIATE_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRoadmapCollection(String deletedCollectionName){
        Result<Boolean> result = client.schema().classDeleter()
                .withClassName(deletedCollectionName)
                .run();

        if(result.hasErrors()) {
            log.error("Failed to delete collection: {}", result.getError().getMessages());
        }
    }
}

