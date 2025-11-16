package com.fpt.careermate.services.recommendation.service;

import com.fpt.careermate.services.resume_services.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

/**
 * Multi-Factor Scoring Service for Candidate Recommendations
 * Implements weighted scoring across multiple qualification dimensions
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class QualificationScoringService {

    // Scoring weights (total = 100%)
    private static final double WEIGHT_SKILLS = 0.40;           // 40% - Most important
    private static final double WEIGHT_EXPERIENCE = 0.25;       // 25% - Second most important
    private static final double WEIGHT_EDUCATION = 0.15;        // 15% - Foundational
    private static final double WEIGHT_CERTIFICATES = 0.10;     // 10% - Professional validation
    private static final double WEIGHT_PROJECTS = 0.05;         // 5% - Practical demonstration
    private static final double WEIGHT_AWARDS = 0.03;           // 3% - Recognition
    private static final double WEIGHT_LANGUAGES = 0.02;        // 2% - Additional skill

    /**
     * Calculate comprehensive qualification score
     *
     * @param resume Candidate's resume with all qualifications
     * @param requiredSkills Required skills from job posting
     * @param minYearsExperience Minimum years of experience required
     * @param semanticScore Semantic similarity score from Weaviate (0.0 - 1.0)
     * @return Combined score (0.0 - 1.0)
     */
    public double calculateComprehensiveScore(
            Resume resume,
            List<String> requiredSkills,
            int minYearsExperience,
            double semanticScore
    ) {
        double totalScore = 0.0;

        // 1. Skills Score (40%) - Combination of exact matching and semantic similarity
        double skillsScore = calculateSkillsScore(resume.getSkills(), requiredSkills, semanticScore);
        totalScore += skillsScore * WEIGHT_SKILLS;

        // 2. Experience Score (25%) - Years and relevance
        double experienceScore = calculateExperienceScore(resume.getWorkExperiences(), minYearsExperience);
        totalScore += experienceScore * WEIGHT_EXPERIENCE;

        // 3. Education Score (15%) - Degree level and field relevance
        double educationScore = calculateEducationScore(resume.getEducations());
        totalScore += educationScore * WEIGHT_EDUCATION;

        // 4. Certificates Score (10%) - Relevant certifications
        double certificatesScore = calculateCertificatesScore(resume.getCertificates());
        totalScore += certificatesScore * WEIGHT_CERTIFICATES;

        // 5. Projects Score (5%) - Highlight projects
        double projectsScore = calculateProjectsScore(resume.getHighlightProjects(), requiredSkills);
        totalScore += projectsScore * WEIGHT_PROJECTS;

        // 6. Awards Score (3%) - Professional recognition
        double awardsScore = calculateAwardsScore(resume.getAwards());
        totalScore += awardsScore * WEIGHT_AWARDS;

        // 7. Languages Score (2%) - Foreign language proficiency
        double languagesScore = calculateLanguagesScore(resume.getForeignLanguages());
        totalScore += languagesScore * WEIGHT_LANGUAGES;

        log.debug("Score breakdown - Skills: {}, Exp: {}, Edu: {}, Certs: {}, Projects: {}, Awards: {}, Langs: {} => Total: {}",
                String.format("%.2f", skillsScore),
                String.format("%.2f", experienceScore),
                String.format("%.2f", educationScore),
                String.format("%.2f", certificatesScore),
                String.format("%.2f", projectsScore),
                String.format("%.2f", awardsScore),
                String.format("%.2f", languagesScore),
                String.format("%.2f", totalScore));

        return Math.min(1.0, totalScore); // Cap at 1.0
    }

    /**
     * Skills Scoring (40% weight)
     * Combines exact skill matching with semantic similarity from embeddings
     *
     * NOTE: Semantic embeddings from Weaviate automatically handle:
     * - Skill synonyms (JavaScript vs JS, React vs ReactJS)
     * - Related technologies (Spring Boot → Java ecosystem)
     * - Niche/emerging technologies (Kafka, GraphQL, Svelte, etc.)
     * - Company-specific tech stacks
     * This eliminates the need for manual skill synonym mapping.
     */
    private double calculateSkillsScore(
            List<Skill> candidateSkills,
            List<String> requiredSkills,
            double semanticScore
    ) {
        if (requiredSkills.isEmpty()) return 1.0;

        Set<String> candidateSkillNames = candidateSkills.stream()
                .map(skill -> skill.getSkillName().toLowerCase())
                .collect(Collectors.toSet());

        // Exact match score
        long exactMatches = requiredSkills.stream()
                .filter(required -> candidateSkillNames.contains(required.toLowerCase()))
                .count();

        double exactMatchRatio = (double) exactMatches / requiredSkills.size();

        // Combined score: 60% exact match + 40% semantic similarity
        // This ensures candidates with exact matches rank higher
        double combined = (exactMatchRatio * 0.6) + (semanticScore * 0.4);

        return Math.min(1.0, combined);
    }

    /**
     * Experience Scoring (25% weight)
     * Based on years of experience and job title relevance
     */
    private double calculateExperienceScore(
            List<WorkExperience> experiences,
            int minYearsRequired
    ) {
        if (experiences == null || experiences.isEmpty()) {
            return minYearsRequired == 0 ? 0.5 : 0.0;
        }

        // Calculate total years of experience
        int totalYears = calculateTotalYears(experiences);

        double baseScore;
        if (minYearsRequired == 0) {
            // No minimum required - any experience is a bonus
            baseScore = Math.min(1.0, 0.5 + (totalYears * 0.1));
        } else if (totalYears >= minYearsRequired) {
            // Meets or exceeds requirement
            baseScore = 0.8 + Math.min(0.2, (totalYears - minYearsRequired) * 0.05);
        } else {
            // Below requirement - partial credit
            baseScore = (totalYears / (double) minYearsRequired) * 0.7;
        }

        // Bonus for senior/leadership roles
        boolean hasSeniorRole = experiences.stream()
                .anyMatch(exp -> exp.getJobTitle() != null &&
                        (exp.getJobTitle().toLowerCase().contains("senior") ||
                         exp.getJobTitle().toLowerCase().contains("lead") ||
                         exp.getJobTitle().toLowerCase().contains("principal")));

        if (hasSeniorRole) {
            baseScore *= 1.1;
        }

        boolean hasLeadershipRole = experiences.stream()
                .anyMatch(exp -> exp.getJobTitle() != null &&
                        (exp.getJobTitle().toLowerCase().contains("manager") ||
                         exp.getJobTitle().toLowerCase().contains("director") ||
                         exp.getJobTitle().toLowerCase().contains("head")));

        if (hasLeadershipRole) {
            baseScore *= 1.05;
        }

        return Math.min(1.0, baseScore);
    }

    /**
     * Education Scoring (15% weight)
     * Based on degree level and field relevance
     */
    private double calculateEducationScore(List<Education> educations) {
        if (educations == null || educations.isEmpty()) {
            return 0.4; // Base score for no education data
        }

        // Degree level scores
        Map<String, Double> degreeScores = Map.of(
                "phd", 1.0,
                "doctor", 1.0,
                "master", 0.9,
                "bachelor", 0.8,
                "associate", 0.6,
                "diploma", 0.5
        );

        double bestScore = educations.stream()
                .map(edu -> {
                    String degree = edu.getDegree() != null ? edu.getDegree().toLowerCase() : "";

                    // Find matching degree score
                    double score = degreeScores.entrySet().stream()
                            .filter(entry -> degree.contains(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(0.5);

                    // Field relevance bonus for tech-related fields (using major field)
                    String major = edu.getMajor() != null ? edu.getMajor().toLowerCase() : "";
                    if (major.contains("computer") || major.contains("software") ||
                        major.contains("information") || major.contains("engineering")) {
                        score *= 1.1; // Tech relevance bonus
                    } else if (major.contains("science") || major.contains("math")) {
                        score *= 1.05; // Related field
                    } else {
                        score *= 0.9; // Other fields
                    }

                    return score;
                })
                .max(Double::compareTo)
                .orElse(0.5);

        return Math.min(1.0, bestScore);
    }

    /**
     * Certificates Scoring (10% weight)
     * Based on relevant certifications
     */
    private double calculateCertificatesScore(List<Certificate> certificates) {
        if (certificates == null || certificates.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);

        for (Certificate cert : certificates) {
            // Base score per certificate
            score += 0.15;

            // Industry-standard certification bonus
            String certName = cert.getName() != null ? cert.getName().toLowerCase() : "";
            if (certName.contains("aws") || certName.contains("azure") ||
                certName.contains("google cloud") || certName.contains("oracle") ||
                certName.contains("cisco") || certName.contains("microsoft")) {
                score += 0.05;
            }

            // Recent certification bonus
            if (cert.getGetDate() != null && cert.getGetDate().isAfter(twoYearsAgo)) {
                score *= 1.1;
            }
        }

        return Math.min(1.0, score);
    }

    /**
     * Projects Scoring (5% weight)
     * Based on relevant project experience
     *
     * NOTE: Project descriptions are included in the candidate's semantic embedding,
     * so the semantic score from Weaviate already captures project relevance.
     * This scoring provides additional granular assessment.
     */
    private double calculateProjectsScore(
            List<HighlightProject> projects,
            List<String> requiredSkills
    ) {
        if (projects == null || projects.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        Set<String> skillKeywords = requiredSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        for (HighlightProject project : projects) {
            String projectText = (project.getDescription() != null ? project.getDescription() : "") + " " +
                                 (project.getName() != null ? project.getName() : "");
            String combinedText = projectText.toLowerCase();

            // Count matching skill keywords in project description
            long matches = skillKeywords.stream()
                    .filter(combinedText::contains)
                    .count();

            if (matches > 5) {
                score += 0.25; // Highly relevant project
            } else if (matches > 2) {
                score += 0.15; // Moderately relevant
            } else if (matches > 0) {
                score += 0.10; // Somewhat relevant
            } else {
                score += 0.05; // Any project shows initiative
            }
        }

        return Math.min(1.0, score);
    }

    /**
     * Awards Scoring (3% weight)
     * Based on professional recognition
     */
    private double calculateAwardsScore(List<Award> awards) {
        if (awards == null || awards.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);

        for (Award award : awards) {
            score += 0.3; // Base per award

            // Recent award bonus
            if (award.getGetDate() != null && award.getGetDate().isAfter(twoYearsAgo)) {
                score *= 1.2;
            }
        }

        return Math.min(1.0, score);
    }

    /**
     * Languages Scoring (2% weight)
     * Based on foreign language proficiency
     */
    private double calculateLanguagesScore(List<ForeignLanguage> languages) {
        if (languages == null || languages.isEmpty()) {
            return 0.0;
        }

        double score = 0.0;

        for (ForeignLanguage lang : languages) {
            String proficiency = lang.getLevel() != null ?
                    lang.getLevel().toLowerCase() : "";

            if (proficiency.contains("native") || proficiency.contains("c2")) {
                score += 0.20;
            } else if (proficiency.contains("advanced") || proficiency.contains("c1")) {
                score += 0.15;
            } else if (proficiency.contains("intermediate") || proficiency.contains("b")) {
                score += 0.10;
            } else {
                score += 0.05;
            }
        }

        return Math.min(1.0, score);
    }

    /**
     * Calculate total years of work experience
     */
    private int calculateTotalYears(List<WorkExperience> experiences) {
        return experiences.stream()
                .mapToInt(exp -> {
                    if (exp.getStartDate() != null && exp.getEndDate() != null) {
                        Period period = Period.between(exp.getStartDate(), exp.getEndDate());
                        return period.getYears();
                    }
                    return 0;
                })
                .sum();
    }
}
