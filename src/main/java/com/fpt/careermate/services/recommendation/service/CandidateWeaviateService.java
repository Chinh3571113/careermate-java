package com.fpt.careermate.services.recommendation.service;

import com.fpt.careermate.services.resume_services.domain.*;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

/**
 * Service for automatically storing and updating candidate profiles in Weaviate
 * Follows the dual-storage pattern from JobPosting implementation
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class CandidateWeaviateService {

    WeaviateClient weaviateClient;

    private static final String CANDIDATE_CLASS = "CandidateProfile";

    /**
     * Automatically store candidate profile in Weaviate after PostgreSQL save
     * Called from ResumeService.createResume() and updateResume()
     */
    public void storeCandidateProfile(Resume resume) {
        try {
            log.info("📝 Storing candidate {} profile in Weaviate", resume.getCandidate().getCandidateId());

            // First, try to delete existing entry to avoid duplicates
            deleteCandidateProfile(resume.getCandidate().getCandidateId());

            // Build comprehensive profile data
            Map<String, Object> properties = buildCandidateProperties(resume);

            // Generate deterministic UUID from candidateId
            String uuid = generateUUID(resume.getCandidate().getCandidateId());

            // Create Weaviate object
            Result<WeaviateObject> result = weaviateClient.data().creator()
                    .withClassName(CANDIDATE_CLASS)
                    .withID(uuid)
                    .withProperties(properties)
                    .run();

            if (result.hasErrors()) {
                log.error("❌ Failed to store candidate {} in Weaviate: {}",
                    resume.getCandidate().getCandidateId(),
                    result.getError().getMessages());
            } else {
                log.info("✅ Successfully stored candidate {} in Weaviate",
                    resume.getCandidate().getCandidateId());
            }

        } catch (Exception e) {
            log.error("❌ Error storing candidate profile in Weaviate: {}", e.getMessage(), e);
            // Don't throw - we don't want to rollback PostgreSQL transaction
        }
    }

    /**
     * Delete candidate profile from Weaviate
     * Called when resume is deleted
     */
    public void deleteCandidateProfile(int candidateId) {
        try {
            String uuid = generateUUID(candidateId);

            Result<Boolean> result = weaviateClient.data().deleter()
                    .withClassName(CANDIDATE_CLASS)
                    .withID(uuid)
                    .run();

            if (result.hasErrors()) {
                log.debug("Candidate {} not found in Weaviate (may not exist yet)", candidateId);
            } else {
                log.info("🗑️ Deleted candidate {} from Weaviate", candidateId);
            }

        } catch (Exception e) {
            log.error("❌ Error deleting candidate from Weaviate: {}", e.getMessage(), e);
        }
    }

    /**
     * Build comprehensive candidate properties for Weaviate storage
     * Includes all resume components with proper weighting
     */
    private Map<String, Object> buildCandidateProperties(Resume resume) {
        Map<String, Object> properties = new HashMap<>();

        // Basic candidate info
        properties.put("candidateId", resume.getCandidate().getCandidateId());
        properties.put("candidateName", resume.getCandidate().getFullName());
        properties.put("email", resume.getCandidate().getAccount().getEmail());
        properties.put("aboutMe", resume.getAboutMe() != null ? resume.getAboutMe() : "");

        // Skills (40% weight) - most important for matching
        List<String> skills = resume.getSkills().stream()
                .map(Skill::getSkillName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        properties.put("skills", skills);

        // Work Experience (25% weight) - comprehensive summary
        String workExperienceSummary = buildWorkExperienceSummary(resume.getWorkExperiences());
        properties.put("workExperienceSummary", workExperienceSummary);
        properties.put("totalExperience", calculateTotalExperience(resume.getWorkExperiences()));

        // Education (15% weight) - formatted summary
        String educationSummary = buildEducationSummary(resume.getEducations());
        properties.put("educationSummary", educationSummary);

        // Certificates (10% weight) - list of certifications
        List<String> certificates = resume.getCertificates().stream()
                .map(cert -> cert.getName() + " (" + cert.getOrganization() + ")")
                .collect(Collectors.toList());
        properties.put("certificates", certificates);

        // Projects (5% weight) - highlight projects with tech stack
        List<String> projects = resume.getHighlightProjects().stream()
                .map(proj -> proj.getName() + ": " +
                    (proj.getDescription() != null ? proj.getDescription() : ""))
                .collect(Collectors.toList());
        properties.put("projects", projects);

        // Awards (3% weight) - professional recognition
        List<String> awards = resume.getAwards().stream()
                .map(award -> award.getName() + " - " + award.getOrganization())
                .collect(Collectors.toList());
        properties.put("awards", awards);

        // Languages (2% weight) - foreign language proficiency
        List<String> languages = resume.getForeignLanguages().stream()
                .map(lang -> lang.getLanguage() + " (" + lang.getLevel() + ")")
                .collect(Collectors.toList());
        properties.put("languages", languages);

        // Combined profile summary for semantic search
        String profileSummary = buildComprehensiveProfileSummary(resume);
        properties.put("profileSummary", profileSummary);

        // Metadata
        properties.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        return properties;
    }

    /**
     * Build work experience summary with job titles and descriptions
     */
    private String buildWorkExperienceSummary(List<WorkExperience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return "";
        }

        return experiences.stream()
                .map(exp -> String.format("%s at %s: %s",
                    exp.getJobTitle(),
                    exp.getCompany(),
                    exp.getDescription() != null ? exp.getDescription() : ""))
                .collect(Collectors.joining(". "));
    }

    /**
     * Calculate total years of experience
     */
    private int calculateTotalExperience(List<WorkExperience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return 0;
        }

        return experiences.stream()
                .mapToInt(exp -> {
                    if (exp.getStartDate() != null && exp.getEndDate() != null) {
                        return exp.getEndDate().getYear() - exp.getStartDate().getYear();
                    }
                    return 0;
                })
                .sum();
    }

    /**
     * Build education summary with degrees and institutions
     */
    private String buildEducationSummary(List<Education> educations) {
        if (educations == null || educations.isEmpty()) {
            return "";
        }

        return educations.stream()
                .map(edu -> String.format("%s in %s from %s",
                    edu.getDegree(),
                    edu.getMajor(),
                    edu.getSchool()))
                .collect(Collectors.joining("; "));
    }

    /**
     * Build comprehensive profile summary for semantic search
     * This is the main text that gets vectorized
     */
    private String buildComprehensiveProfileSummary(Resume resume) {
        StringBuilder summary = new StringBuilder();

        // About Me
        if (resume.getAboutMe() != null && !resume.getAboutMe().isEmpty()) {
            summary.append(resume.getAboutMe()).append(" ");
        }

        // Skills
        if (!resume.getSkills().isEmpty()) {
            summary.append("Skills: ")
                    .append(resume.getSkills().stream()
                            .map(Skill::getSkillName)
                            .collect(Collectors.joining(", ")))
                    .append(". ");
        }

        // Work Experience
        if (!resume.getWorkExperiences().isEmpty()) {
            summary.append("Experience: ")
                    .append(buildWorkExperienceSummary(resume.getWorkExperiences()))
                    .append(". ");
        }

        // Education
        if (!resume.getEducations().isEmpty()) {
            summary.append("Education: ")
                    .append(buildEducationSummary(resume.getEducations()))
                    .append(". ");
        }

        // Projects
        if (!resume.getHighlightProjects().isEmpty()) {
            summary.append("Projects: ")
                    .append(resume.getHighlightProjects().stream()
                            .map(proj -> proj.getName() + " - " + proj.getDescription())
                            .collect(Collectors.joining("; ")))
                    .append(". ");
        }

        return summary.toString().trim();
    }

    /**
     * Generate deterministic UUID from candidate ID
     * Same candidate ID always produces same UUID
     */
    private String generateUUID(int candidateId) {
        // Use MD5 hash of candidateId to generate consistent UUID
        String input = "candidate-" + candidateId;
        return UUID.nameUUIDFromBytes(input.getBytes()).toString();
    }
}

