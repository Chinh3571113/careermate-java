package com.fpt.careermate.services.coach_services.service;

import com.fpt.careermate.common.constant.ResumeSubtopicProgressStatus;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.coach_services.domain.*;
import com.fpt.careermate.services.coach_services.repository.*;
import com.fpt.careermate.services.coach_services.service.dto.response.*;
import com.fpt.careermate.services.coach_services.service.impl.RoadmapService;
import com.fpt.careermate.services.coach_services.service.mapper.RoadmapMapper;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.resume_services.domain.Resume;
import com.fpt.careermate.services.resume_services.domain.Skill;
import com.fpt.careermate.services.resume_services.repository.ResumeRepo;
import com.fpt.careermate.services.resume_services.repository.SkillRepo;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoadmapImp implements RoadmapService {

    WeaviateClient client;
    RoadmapRepo roadmapRepo;
    TopicRepo topicRepo;
    SubtopicRepo subtopicRepo;
    SkillRepo skillRepo;
    ResumeRepo resumeRepo;
    ResumeSubtopicProgressRepo resumeSubtopicProgressRepo;
    RoadmapMapper roadmapMapper;
    ResumeRoadmapRepo resumeRoadmapRepo;
    CoachUtil coachUtil;
    RoadmapRedisImp roadmapRedisImp;
    EmbeddingImp embeddingImp;

    static String roadmapCollection = "Roadmap";
    static String roadmapCollection2 = "Roadmap2";

    // Lấy roadmap detail từ Postgres
    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public RoadmapResponse getRoadmap(String roadmapName) {
        Roadmap roadmap = roadmapRepo.findByNameContainingIgnoreCase(roadmapName)
                .orElseThrow(() -> new AppException(ErrorCode.ROADMAP_NOT_FOUND));

        return roadmapMapper.toRoadmapResponse(roadmap);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public TopicDetailResponse getTopicDetail(int topicId) {
        // Kiểm tra topic tồn tại
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
        String[] urls = topic.getResources().split(",");
        List<ResourceResponse> resourceResponses = new ArrayList<>();

        TopicDetailResponse topicDetailResponse = roadmapMapper.topicDetailResponse(topic);

        // Map urls to ResourceResponse
        for (String url : urls) {
            ResourceResponse resourceResponse = new ResourceResponse(url.trim());
            resourceResponses.add(resourceResponse);
        }

        topicDetailResponse.setResourceResponses(resourceResponses);
        return topicDetailResponse;
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public TopicDetailResponse getSubtopicDetail(int subtopicId) {
        // Kiểm tra subtopic tồn tại
        Subtopic subtopic = subtopicRepo.findById(subtopicId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBTOPIC_NOT_FOUND));
        String[] urls = subtopic.getResources().split(",");
        List<ResourceResponse> resourceResponses = new ArrayList<>();

        TopicDetailResponse topicDetailResponse = roadmapMapper.toSubtopicDetailResponse(subtopic);

        // Map urls to ResourceResponse
        for (String url : urls) {
            ResourceResponse resourceResponse = new ResourceResponse(url.trim());
            resourceResponses.add(resourceResponse);
        }

        topicDetailResponse.setResourceResponses(resourceResponses);
        return topicDetailResponse;
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public List<RecommendedRoadmapResponse> recommendRoadmap(String role) {
        // Tạo bộ lọc tìm kiếm gần theo văn bản (nearText)
        // "concepts" là mảng các từ khóa hoặc cụm từ dùng để tìm kiếm ngữ nghĩa
        // "certainty" là ngưỡng độ tin cậy tối thiểu của kết quả (0.7f = 70%)
        NearTextArgument nearText = NearTextArgument.builder()
                // vì SDK được sinh máy móc từ định nghĩa GraphQL, nên nó phản ánh y nguyên kiểu danh sách.
                .concepts(new String[]{role.toUpperCase().trim()})
                .certainty(0.71f)
                .build();

        // Xác định các trường cần lấy từ đối tượng "Roadmap" trong Weaviate
        // Bao gồm: "name" và "_additional.certainty" (độ tương tự)
        Fields fields = Fields.builder()
                .fields(new Field[]{
                        Field.builder().name("name").build(),
                        Field.builder().name("_additional").fields(new Field[]{
                                Field.builder().name("certainty").build()
                        }).build()
                })
                .build();

        // Tạo câu truy vấn GraphQL để lấy danh sách 3 roadmap liên quan nhất
        String query = GetBuilder.builder()
                .className(roadmapCollection)
                .fields(fields)                 // các trường cần lấy
                .withNearTextFilter(nearText)   // áp dụng bộ lọc nearText
                .limit(3)
                .build()
                .buildQuery();

        // Gửi truy vấn GraphQL đến Weaviate và nhận kết quả trả về
        // tự viết câu truy vấn GraphQL dạng chuỗi (query)
        Result<GraphQLResponse> result = client.graphQL().raw().withQuery(query).run();
        GraphQLResponse graphQLResponse = result.getResult();

        // Trích xuất dữ liệu từ phản hồi GraphQL (ở dạng Map lồng nhau)
        Map<String, Object> data = (Map<String, Object>) graphQLResponse.getData();   // {Get={Roadmap=[{...}]}}
        Map<String, Object> get = (Map<String, Object>) data.get("Get");              // {Roadmap=[{...}]}
        List<Map<String, Object>> RoadmapData = (List<Map<String, Object>>) get.get(roadmapCollection);

        // Chuyển từng phần tử trong danh sách sang đối tượng phản hồi (DTO)
        List<RecommendedRoadmapResponse> recommendedRoadmapResponseList = new ArrayList<>();
        RoadmapData.forEach(roadmap -> {
            String name = (String) roadmap.get("name");
            Map<String, Object> additional = (Map<String, Object>) roadmap.get("_additional");
            Double similarityScore = (Double) additional.get("certainty");

            // Thêm vào danh sách kết quả trả về
            recommendedRoadmapResponseList.add(new RecommendedRoadmapResponse(name, similarityScore));
        });

        // Trả về danh sách roadmap gợi ý
        return recommendedRoadmapResponseList;
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    @Transactional
    public void highlightedResume(int resumeId) {
        long startTime = System.currentTimeMillis();
        Candidate candidate = coachUtil.getCurrentCandidate();

        // Lấy resume và validate ownership
        Resume resume = resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));

        // Lấy skills từ resume và validate
        List<Skill> skills = skillRepo.findByResume_ResumeId(resumeId);
        if (skills.isEmpty()) {
            log.warn("Resume {} has no skills, cannot recommend roadmap", resumeId);
            throw new AppException(ErrorCode.RESUME_HAS_NO_SKILLS);
        }

        // Tìm roadmap phù hợp nhất dựa trên skills
        String roadmapName = findRoadmapBySkills(skills);
        Roadmap roadmap = roadmapRepo.findByNameContainingIgnoreCase(roadmapName)
                .orElseThrow(() -> new AppException(ErrorCode.ROADMAP_NOT_FOUND));

        // Kiểm tra xem resume đã chọn roadmap này chưa
        Optional<ResumeRoadmap> existingResumeRoadmap = resumeRoadmapRepo.findByResume_ResumeIdAndRoadmap_Id(
                resume.getResumeId(),
                roadmap.getId()
        );

        ResumeRoadmap savedResumeRoadmap;
        boolean isNewRoadmap;

        if (existingResumeRoadmap.isPresent()) {
            // Nếu đã tồn tại, sử dụng ResumeRoadmap hiện có để thêm progress mới
            savedResumeRoadmap = existingResumeRoadmap.get();
            isNewRoadmap = false;
            log.info("Resume {} already has roadmap {}, adding new progress only", resumeId, roadmap.getName());
        } else {
            // Tạo liên kết resume-roadmap mới
            ResumeRoadmap resumeRoadmap = ResumeRoadmap.builder()
                    .resume(resume)
                    .roadmap(roadmap)
                    .isActive(true)
                    .build();
            savedResumeRoadmap = resumeRoadmapRepo.save(resumeRoadmap);
            isNewRoadmap = true;
            log.info("Created new roadmap {} for resume {}", roadmap.getName(), resumeId);

            // Invalidate both candidate-level and resume-specific caches (chỉ khi tạo mới)
            roadmapRedisImp.invalidateResumeRoadmapListCache(candidate.getCandidateId());
            roadmapRedisImp.invalidateSpecificResumeRoadmapListCache(resumeId);
        }

        // Match skills với subtopics và đánh dấu progress
        List<ResumeSubtopicProgress> resumeSubtopicProgresses = matchSkillsWithSubtopics(
                skills,
                roadmap,
                savedResumeRoadmap
        );

        // Nếu là update (không phải new), lọc ra chỉ những progress mới chưa tồn tại
        if (!isNewRoadmap && !resumeSubtopicProgresses.isEmpty()) {
            // Lấy danh sách subtopic IDs đã có progress
            List<ResumeSubtopicProgress> existingProgresses = resumeSubtopicProgressRepo
                    .findAllByResumeRoadmap_Id(savedResumeRoadmap.getId());
            Set<Integer> existingSubtopicIds = existingProgresses.stream()
                    .map(progress -> progress.getSubtopic().getId())
                    .collect(Collectors.toSet());

            // Chỉ giữ lại những progress của subtopics chưa có
            int originalSize = resumeSubtopicProgresses.size();
            resumeSubtopicProgresses = resumeSubtopicProgresses.stream()
                    .filter(progress -> !existingSubtopicIds.contains(progress.getSubtopic().getId()))
                    .collect(Collectors.toList());

            log.info("Filtered out {} existing progress entries, will add {} new entries",
                    originalSize - resumeSubtopicProgresses.size(), resumeSubtopicProgresses.size());
        }

        // Lưu progress mới vào database
        if (!resumeSubtopicProgresses.isEmpty()) {
            try {
                resumeSubtopicProgressRepo.saveAll(resumeSubtopicProgresses);
                if (isNewRoadmap) {
                    log.info("Saved {} completed subtopics for new roadmap in resume {}",
                            resumeSubtopicProgresses.size(), resumeId);
                } else {
                    log.info("Added {} new completed subtopics for existing roadmap in resume {}",
                            resumeSubtopicProgresses.size(), resumeId);
                }
            } catch (Exception e) {
                log.error("Error saving ResumeSubtopicProgress: {}", e.getMessage(), e);
                throw new AppException(ErrorCode.RESUME_SUBPTOPIC_PROGRESS_CREATE_FAIL);
            }
        } else {
            if (isNewRoadmap) {
                log.info("No matching subtopics found for resume {} skills", resumeId);
            } else {
                log.info("No new matching subtopics to add for resume {}", resumeId);
            }
        }

        // Log thời gian thực thi
        long endTime = System.currentTimeMillis();
        log.info("Roadmap recommendation process completed in {} ms", (endTime - startTime));
    }

    /**
     * Match skills với subtopics trong roadmap và tạo progress records.
     * Sử dụng caching để tối ưu performance khi tính embedding.
     */
    private List<ResumeSubtopicProgress> matchSkillsWithSubtopics(
            List<Skill> skills,
            Roadmap roadmap,
            ResumeRoadmap resumeRoadmap
    ) {
        List<ResumeSubtopicProgress> progresses = new ArrayList<>();
        Set<Integer> matchedSubtopicIds = new HashSet<>(); // Tránh duplicate

        // Pre-compute embeddings cho tất cả skills (cache để tránh tính lại)
        Map<String, float[]> skillEmbeddings = new HashMap<>();
        for (Skill skill : skills) {
            String skillName = skill.getSkillName().toLowerCase().trim();
            try {
                float[] embedding = embeddingImp.embed(skillName);
                if (embedding.length > 0) {
                    skillEmbeddings.put(skillName, embedding);
                }
            } catch (Exception e) {
                log.warn("Failed to create embedding for skill: {}", skillName, e);
            }
        }

        OffsetDateTime now = OffsetDateTime.now();

        // Duyệt qua tất cả subtopics trong roadmap
        for (Topic topic : roadmap.getTopics()) {
            for (Subtopic subtopic : topic.getSubtopics()) {
                // Skip nếu đã match rồi
                if (matchedSubtopicIds.contains(subtopic.getId())) {
                    continue;
                }

                String subtopicName = subtopic.getName().toLowerCase().trim();
                boolean matched = false;

                // Case 1: Exact name match (case-insensitive)
                for (Skill skill : skills) {
                    if (skill.getSkillName().equalsIgnoreCase(subtopicName)) {
                        matched = true;
                        break;
                    }
                }

                // Case 2: Semantic similarity using embeddings
                if (!matched) {
                    try {
                        float[] subtopicEmbedding = embeddingImp.embed(subtopicName);
                        if (subtopicEmbedding.length > 0) {
                            for (Map.Entry<String, float[]> entry : skillEmbeddings.entrySet()) {
                                double similarity = embeddingImp.cosineSimilarity(
                                        subtopicEmbedding,
                                        entry.getValue()
                                );
                                if (similarity >= 0.8) {
                                    matched = true;
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to compute similarity for subtopic: {}", subtopicName, e);
                    }
                }

                // Nếu matched thì tạo progress record
                if (matched) {
                    ResumeSubtopicProgress progress = ResumeSubtopicProgress.builder()
                            .resumeRoadmap(resumeRoadmap)
                            .subtopic(subtopic)
                            .updatedAt(now)
                            .status(ResumeSubtopicProgressStatus.COMPLETED)
                            .build();
                    progresses.add(progress);
                    matchedSubtopicIds.add(subtopic.getId());
                }
            }
        }

        return progresses;
    }

    // Tìm Roadmap dựa theo skills được lấy từ resume
    private String findRoadmapBySkills(List<Skill> skills) {
        // Chuyển list skills thành mảng string để tìm kiếm
        String[] skillNames = skills.stream()
                .map(Skill::getSkillName)
                .toArray(String[]::new);

        // Tạo bộ lọc tìm kiếm gần theo văn bản (nearText) dựa trên skills
        // "concepts" là mảng các skills dùng để tìm kiếm ngữ nghĩa
        // "certainty" là ngưỡng độ tin cậy tối thiểu của kết quả (0.7f = 70%)
        NearTextArgument nearText = NearTextArgument.builder()
                .concepts(skillNames)
                .certainty(0.55f)
                .build();

        // Xác định các trường cần lấy từ đối tượng "Roadmap" trong Weaviate
        // Bao gồm: "name" và "_additional.certainty" (độ tương tự)
        Fields fields = Fields.builder()
                .fields(new Field[]{
                        Field.builder().name("name").build(),
                        Field.builder().name("_additional").fields(new Field[]{
                                Field.builder().name("certainty").build()
                        }).build()
                })
                .build();

        // Tạo câu truy vấn GraphQL để lấy roadmap phù hợp nhất
        String query = GetBuilder.builder()
                .className(roadmapCollection2)
                .fields(fields)                 // các trường cần lấy
                .withNearTextFilter(nearText)   // áp dụng bộ lọc nearText dựa trên skills
                .limit(1)                       // chỉ lấy roadmap phù hợp nhất
                .build()
                .buildQuery();

        // Gửi truy vấn GraphQL đến Weaviate và nhận kết quả trả về
        Result<GraphQLResponse> result = client.graphQL().raw().withQuery(query).run();

        if (result.hasErrors()) {
            log.error("Error querying Weaviate: {}", result.getError().getMessages());
            throw new AppException(ErrorCode.WEAVIATE_ERROR);
        }

        GraphQLResponse graphQLResponse = result.getResult();

        // Trích xuất dữ liệu từ phản hồi GraphQL (ở dạng Map lồng nhau)
        Map<String, Object> data = (Map<String, Object>) graphQLResponse.getData();
        Map<String, Object> get = (Map<String, Object>) data.get("Get");
        List<Map<String, Object>> roadmapData = (List<Map<String, Object>>) get.get(roadmapCollection2);

        // Kiểm tra có roadmap nào được tìm thấy không
        if (roadmapData == null || roadmapData.isEmpty()) {
            log.warn("No roadmap found matching skills: {}", String.join(", ", skillNames));
            throw new AppException(ErrorCode.ROADMAP_NOT_FOUND);
        }

        // Lấy tên roadmap phù hợp nhất
        String roadmapName = (String) roadmapData.get(0).get("name");

        // Log thông tin để debug
        Map<String, Object> additional = (Map<String, Object>) roadmapData.get(0).get("_additional");
        Double certainty = (Double) additional.get("certainty");
        log.info("Found roadmap '{}' with certainty: {} based on skills: {}",
                roadmapName, certainty, String.join(", ", skillNames));

        return roadmapName;
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public RoadmapResponse getCandidateRoadmap(int resumeId, String roadmapName) {
        long startTime = System.currentTimeMillis();

        // Try to get from Redis cache first
        RoadmapResponse cachedResponse = roadmapRedisImp.getCandidateRoadmapFromCache(resumeId, roadmapName);
        if (cachedResponse != null) {
            long endTime = System.currentTimeMillis();
            log.info("Get candidate roadmap from cache took {} ms", (endTime - startTime));
            return cachedResponse;
        }

        // Cache miss - fetch from database
        Roadmap roadmap = roadmapRepo.findByNameContainingIgnoreCase(roadmapName)
                .orElseThrow(() -> new AppException(ErrorCode.ROADMAP_NOT_FOUND));
        Optional<ResumeRoadmap> resumeRoadmapOptional = resumeRoadmapRepo.findByResume_ResumeIdAndRoadmap_Id(
                resumeId,
                roadmap.getId()
        );
        if(resumeRoadmapOptional.isEmpty()) throw new AppException(ErrorCode.RESUME_ROADMAP_NOT_FOUND);
        ResumeRoadmap resumeRoadmap = resumeRoadmapOptional.get();

        RoadmapResponse roadmapResponse = roadmapMapper.toRoadmapResponse(roadmap);

        // Lấy subtopic đã học của candidate từ resumeRoadmapId
        List<ResumeSubtopicProgress> progresses =
                resumeSubtopicProgressRepo.findAllByResumeRoadmap_Id(resumeRoadmap.getId());

        // Build map: subtopicId -> status
        Map<Integer, ResumeSubtopicProgressStatus> statusMap =
                progresses.stream()
                        .collect(Collectors.toMap(
                                p -> p.getSubtopic().getId(),
                                ResumeSubtopicProgress::getStatus
                        ));

        for (TopicResponse topic : roadmapResponse.getTopics()) {
            for (SubtopicResponse subtopic : topic.getSubtopics()) {
                ResumeSubtopicProgressStatus status = statusMap.get(subtopic.getId());

                if (status != null) {
                    subtopic.setStatus(status);
                }
            }
        }

        // Save to Redis cache
        roadmapRedisImp.saveCandidateRoadmapToCache(resumeId, roadmapName, roadmapResponse);

        // Log thời gian thực thi
        long endTime = System.currentTimeMillis();
        log.info("Get candidate roadmap from DB took {} ms", (endTime - startTime));
        return roadmapResponse;
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResumeRoadmapPageResponse getMyRoadmapListOfAResume(Integer resumeId, int page, int size, String sortBy) {
        long startTime = System.currentTimeMillis();

        // Get current candidate
        Candidate candidate = coachUtil.getCurrentCandidate();
        int candidateId = candidate.getCandidateId();

        // Try to get from Redis cache first
        String cacheKey = resumeId != null ? "resume:" + resumeId : "candidate:" + candidateId;
        ResumeRoadmapPageResponse cachedResponse = resumeId != null
            ? roadmapRedisImp.getResumeRoadmapListFromCache(resumeId, page, size, sortBy, true)
            : roadmapRedisImp.getResumeRoadmapListFromCache(candidateId, page, size, sortBy);

        if (cachedResponse != null) {
            long endTime = System.currentTimeMillis();
            log.info("Get roadmap list for {} from cache took {} ms", cacheKey, (endTime - startTime));
            return cachedResponse;
        }

        // Build Sort object based on sortBy parameter
        Sort sort;
        switch (sortBy.toLowerCase()) {
            case "createdat_asc":
                sort = Sort.by(Sort.Direction.ASC, "createdAt");
                break;
            case "createdat_desc":
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
            case "roadmapname_asc":
                sort = Sort.by(Sort.Direction.ASC, "roadmap.name");
                break;
            case "roadmapname_desc":
                sort = Sort.by(Sort.Direction.DESC, "roadmap.name");
                break;
            default:
                // Default sort by createdAt descending
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        // Fetch paginated data based on whether resumeId is provided
        Page<ResumeRoadmap> resumeRoadmapPage;
        if (resumeId != null) {
            // Validate ownership
            resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidateId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
            // Get roadmaps for specific resume
            resumeRoadmapPage = resumeRoadmapRepo.findByResume_ResumeId(resumeId, pageable);
        } else {
            // Get roadmaps for all resumes of this candidate
            resumeRoadmapPage = resumeRoadmapRepo.findByResume_Candidate_CandidateId(candidateId, pageable);
        }

        // Map to response DTO
        List<ResumeRoadmapListResponse> content = resumeRoadmapPage.getContent().stream()
                .map(resumeRoadmap -> ResumeRoadmapListResponse.builder()
                        .id(resumeRoadmap.getId())
                        .resumeId(resumeRoadmap.getResume().getResumeId())
                        .roadmapName(resumeRoadmap.getRoadmap().getName())
                        .isActive(resumeRoadmap.isActive())
                        .createdAt(resumeRoadmap.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // Build response
        ResumeRoadmapPageResponse response = ResumeRoadmapPageResponse.builder()
                .content(content)
                .number(resumeRoadmapPage.getNumber())
                .size(resumeRoadmapPage.getSize())
                .totalElements(resumeRoadmapPage.getTotalElements())
                .totalPages(resumeRoadmapPage.getTotalPages())
                .first(resumeRoadmapPage.isFirst())
                .last(resumeRoadmapPage.isLast())
                .build();

        // Save to Redis cache
        if (resumeId != null) {
            roadmapRedisImp.saveResumeRoadmapListToCache(resumeId, page, size, sortBy, response, true);
        } else {
            roadmapRedisImp.saveResumeRoadmapListToCache(candidateId, page, size, sortBy, response);
        }

        // Log thời gian thực thi
        long endTime = System.currentTimeMillis();
        log.info("Get roadmap list for {} from DB took {} ms", cacheKey, (endTime - startTime));

        return response;
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    @Transactional
    public void toggleSubtopicProgressStatus(int resumeId, int subtopicId) {
        long startTime = System.currentTimeMillis();

        // Get current candidate
        Candidate candidate = coachUtil.getCurrentCandidate();

        // Validate ownership: check if the resume belongs to current candidate
        Resume resume = resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));

        // Find or create progress entry by resumeId and subtopicId
        Optional<ResumeSubtopicProgress> progressOptional = resumeSubtopicProgressRepo
                .findByResumeRoadmap_Resume_ResumeIdAndSubtopic_Id(resumeId, subtopicId);

        ResumeSubtopicProgress progress;
        ResumeSubtopicProgressStatus currentStatus;
        ResumeSubtopicProgressStatus newStatus;

        if (progressOptional.isPresent()) {
            // Progress exists - toggle status
            progress = progressOptional.get();
            currentStatus = progress.getStatus();

            if (currentStatus == ResumeSubtopicProgressStatus.NOT_STARTED) {
                newStatus = ResumeSubtopicProgressStatus.COMPLETED;
            } else if (currentStatus == ResumeSubtopicProgressStatus.COMPLETED) {
                newStatus = ResumeSubtopicProgressStatus.NOT_STARTED;
            } else {
                // If status is IN_PROGRESS, toggle to COMPLETED
                newStatus = ResumeSubtopicProgressStatus.COMPLETED;
            }

            progress.setStatus(newStatus);
            progress.setUpdatedAt(OffsetDateTime.now());
        } else {
            // Progress doesn't exist - create new with COMPLETED status
            // First, need to find the subtopic and resume roadmap
            Subtopic subtopic = subtopicRepo.findById(subtopicId)
                    .orElseThrow(() -> new AppException(ErrorCode.SUBTOPIC_NOT_FOUND));

            // Find ResumeRoadmap for this resume and the roadmap containing this subtopic
            Roadmap roadmap = subtopic.getTopic().getRoadmap();
            ResumeRoadmap resumeRoadmap = resumeRoadmapRepo.findByResume_ResumeIdAndRoadmap_Id(resumeId, roadmap.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESUME_ROADMAP_NOT_FOUND));

            // Create new progress with COMPLETED status
            currentStatus = null; // No previous status
            newStatus = ResumeSubtopicProgressStatus.COMPLETED;

            progress = ResumeSubtopicProgress.builder()
                    .resumeRoadmap(resumeRoadmap)
                    .subtopic(subtopic)
                    .status(newStatus)
                    .updatedAt(OffsetDateTime.now())
                    .build();

            log.info("Created new progress for resume {} and subtopic {} with status COMPLETED", resumeId, subtopicId);
        }

        // Save changes
        resumeSubtopicProgressRepo.save(progress);

        // Invalidate candidate roadmap cache since progress status changed
        String roadmapName = progress.getResumeRoadmap().getRoadmap().getName();
        roadmapRedisImp.invalidateCandidateRoadmapCache(resumeId, roadmapName);
        log.info("Invalidated cache for resume {} and roadmap {}", resumeId, roadmapName);

        // Log thời gian thực thi
        long endTime = System.currentTimeMillis();
        if (currentStatus != null) {
            log.info("Toggled subtopic progress for resume {} and subtopic {} from {} to {} in {} ms",
                    resumeId, subtopicId, currentStatus, newStatus, (endTime - startTime));
        } else {
            log.info("Created subtopic progress for resume {} and subtopic {} with status {} in {} ms",
                    resumeId, subtopicId, newStatus, (endTime - startTime));
        }
    }
}


