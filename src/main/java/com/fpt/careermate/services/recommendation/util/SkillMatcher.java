package com.fpt.careermate.services.recommendation.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for intelligent skill matching with synonym and hierarchy support
 */
@Slf4j
@Component
public class SkillMatcher {

    // Skill synonym mappings (lowercase for case-insensitive matching)
    private static final Map<String, Set<String>> SKILL_SYNONYMS = new HashMap<>();

    // Skill hierarchy (parent -> children skills)
    private static final Map<String, Set<String>> SKILL_HIERARCHY = new HashMap<>();

    static {
        initializeSkillSynonyms();
        initializeSkillHierarchy();
    }

    private static void initializeSkillSynonyms() {
        // Programming Languages
        addSynonyms("javascript", "js", "javascript", "ecmascript", "es6", "es2015");
        addSynonyms("typescript", "ts", "typescript");
        addSynonyms("python", "python", "python3", "py");
        addSynonyms("java", "java", "java8", "java11", "java17", "java21");
        addSynonyms("c#", "c#", "csharp", "c sharp", ".net");
        addSynonyms("c++", "c++", "cpp", "cplusplus");
        addSynonyms("golang", "go", "golang");
        addSynonyms("ruby", "ruby", "ruby on rails", "rails", "ror");
        addSynonyms("php", "php", "php7", "php8");

        // Frontend Frameworks
        addSynonyms("react", "react", "reactjs", "react.js");
        addSynonyms("vue", "vue", "vuejs", "vue.js");
        addSynonyms("angular", "angular", "angularjs", "angular2+");
        addSynonyms("next.js", "next", "nextjs", "next.js");
        addSynonyms("nuxt", "nuxt", "nuxtjs", "nuxt.js");

        // Backend Frameworks
        addSynonyms("spring", "spring", "spring boot", "spring framework", "springboot");
        addSynonyms("express", "express", "expressjs", "express.js");
        addSynonyms("django", "django", "django rest framework", "drf");
        addSynonyms("flask", "flask", "flask framework");
        addSynonyms("nest.js", "nest", "nestjs", "nest.js");
        addSynonyms("fastapi", "fastapi", "fast api");

        // Databases
        addSynonyms("postgresql", "postgres", "postgresql", "psql");
        addSynonyms("mysql", "mysql", "mariadb");
        addSynonyms("mongodb", "mongo", "mongodb");
        addSynonyms("sql server", "sql server", "mssql", "microsoft sql server");
        addSynonyms("oracle", "oracle", "oracle db", "oracle database");
        addSynonyms("redis", "redis", "redis cache");

        // Cloud Platforms
        addSynonyms("aws", "aws", "amazon web services");
        addSynonyms("azure", "azure", "microsoft azure");
        addSynonyms("gcp", "gcp", "google cloud", "google cloud platform");

        // DevOps & Tools
        addSynonyms("docker", "docker", "containerization", "containers");
        addSynonyms("kubernetes", "kubernetes", "k8s");
        addSynonyms("jenkins", "jenkins", "ci/cd");
        addSynonyms("git", "git", "github", "gitlab", "version control");
        addSynonyms("terraform", "terraform", "iac", "infrastructure as code");

        // Testing
        addSynonyms("junit", "junit", "unit testing", "java testing");
        addSynonyms("jest", "jest", "javascript testing");
        addSynonyms("pytest", "pytest", "python testing");
        addSynonyms("selenium", "selenium", "automation testing", "e2e testing");

        // Soft Skills
        addSynonyms("teamwork", "teamwork", "team work", "collaboration", "team collaboration");
        addSynonyms("communication", "communication", "verbal communication", "written communication");
        addSynonyms("problem solving", "problem solving", "problem-solving", "critical thinking");
        addSynonyms("leadership", "leadership", "team lead", "team leadership");
        addSynonyms("agile", "agile", "scrum", "agile methodology");
    }

    private static void initializeSkillHierarchy() {
        // Frontend Development
        addHierarchy("frontend development",
            "html", "css", "javascript", "typescript", "react", "vue", "angular");

        // Backend Development
        addHierarchy("backend development",
            "java", "python", "node.js", "go", "c#", "ruby", "php");

        // Java Ecosystem
        addHierarchy("java",
            "spring", "hibernate", "maven", "gradle", "junit");

        // JavaScript Ecosystem
        addHierarchy("javascript",
            "react", "vue", "angular", "node.js", "express", "next.js");

        // Python Ecosystem
        addHierarchy("python",
            "django", "flask", "fastapi", "pandas", "numpy", "pytest");

        // Database Skills
        addHierarchy("database",
            "sql", "postgresql", "mysql", "mongodb", "redis", "oracle");

        // Cloud Skills
        addHierarchy("cloud computing",
            "aws", "azure", "gcp", "docker", "kubernetes", "terraform");

        // DevOps
        addHierarchy("devops",
            "docker", "kubernetes", "jenkins", "ci/cd", "terraform", "ansible");
    }

    private static void addSynonyms(String canonical, String... variants) {
        Set<String> synonymSet = new HashSet<>(Arrays.asList(variants));
        for (String variant : variants) {
            SKILL_SYNONYMS.put(variant.toLowerCase(), synonymSet.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));
        }
    }

    private static void addHierarchy(String parent, String... children) {
        SKILL_HIERARCHY.put(parent.toLowerCase(),
            Arrays.stream(children)
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));
    }

    /**
     * Normalize a skill to its canonical form
     */
    public String normalizeSkill(String skill) {
        if (skill == null) return null;
        String normalized = skill.toLowerCase().trim();

        // Return the first (canonical) synonym if found
        Set<String> synonyms = SKILL_SYNONYMS.get(normalized);
        if (synonyms != null && !synonyms.isEmpty()) {
            return synonyms.iterator().next();
        }

        return normalized;
    }

    /**
     * Check if two skills match (including synonyms)
     */
    public boolean skillsMatch(String skill1, String skill2) {
        if (skill1 == null || skill2 == null) return false;

        String normalized1 = skill1.toLowerCase().trim();
        String normalized2 = skill2.toLowerCase().trim();

        // Exact match
        if (normalized1.equals(normalized2)) {
            return true;
        }

        // Check if they're synonyms
        Set<String> synonyms1 = SKILL_SYNONYMS.get(normalized1);
        Set<String> synonyms2 = SKILL_SYNONYMS.get(normalized2);

        if (synonyms1 != null && synonyms2 != null) {
            // If they share any synonyms, they match
            return !Collections.disjoint(synonyms1, synonyms2);
        }

        return false;
    }

    /**
     * Find all matching skills from candidate's skills that match required skills
     */
    public Set<String> findMatchingSkills(List<String> requiredSkills, List<String> candidateSkills) {
        Set<String> matches = new HashSet<>();

        for (String required : requiredSkills) {
            for (String candidate : candidateSkills) {
                if (skillsMatch(required, candidate)) {
                    matches.add(required);
                    break; // Found a match for this required skill
                }
            }
        }

        return matches;
    }

    /**
     * Find missing skills (required skills not in candidate's skills)
     */
    public Set<String> findMissingSkills(List<String> requiredSkills, List<String> candidateSkills) {
        Set<String> missing = new HashSet<>(requiredSkills);
        Set<String> matched = findMatchingSkills(requiredSkills, candidateSkills);
        missing.removeAll(matched);
        return missing;
    }

    /**
     * Calculate match score with synonym support
     */
    public double calculateMatchScore(List<String> requiredSkills, List<String> candidateSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return 0.0;
        }

        Set<String> matched = findMatchingSkills(requiredSkills, candidateSkills);
        return (double) matched.size() / requiredSkills.size();
    }

    /**
     * Get bonus score for having parent skills
     * E.g., if job requires "React" and candidate has "JavaScript", give bonus
     */
    public double calculateHierarchyBonus(List<String> requiredSkills, List<String> candidateSkills) {
        double bonus = 0.0;

        for (String required : requiredSkills) {
            String normalizedRequired = required.toLowerCase().trim();

            // Check if candidate has parent skill of the required skill
            for (Map.Entry<String, Set<String>> entry : SKILL_HIERARCHY.entrySet()) {
                Set<String> children = entry.getValue();
                if (children.contains(normalizedRequired)) {
                    // This is a child skill, check if candidate has parent
                    String parent = entry.getKey();
                    for (String candidate : candidateSkills) {
                        if (skillsMatch(parent, candidate)) {
                            bonus += 0.1; // 10% bonus for having parent skill
                            break;
                        }
                    }
                }
            }
        }

        return Math.min(bonus, 0.3); // Cap bonus at 30%
    }

    /**
     * Enhanced match score with hierarchy bonus
     */
    public double calculateEnhancedMatchScore(List<String> requiredSkills, List<String> candidateSkills) {
        double baseScore = calculateMatchScore(requiredSkills, candidateSkills);
        double hierarchyBonus = calculateHierarchyBonus(requiredSkills, candidateSkills);
        return Math.min(baseScore + hierarchyBonus, 1.0);
    }

    /**
     * Get all synonyms for a skill
     */
    public Set<String> getSynonyms(String skill) {
        if (skill == null) return Collections.emptySet();
        return SKILL_SYNONYMS.getOrDefault(skill.toLowerCase().trim(), Collections.emptySet());
    }

    /**
     * Log matching details for debugging
     */
    public void logMatchDetails(String jobTitle, List<String> requiredSkills, List<String> candidateSkills) {
        log.info("🔍 Skill Matching for job: {}", jobTitle);
        log.info("Required skills: {}", requiredSkills);
        log.info("Candidate skills: {}", candidateSkills);

        Set<String> matched = findMatchingSkills(requiredSkills, candidateSkills);
        Set<String> missing = findMissingSkills(requiredSkills, candidateSkills);

        log.info("✅ Matched skills: {}", matched);
        log.info("❌ Missing skills: {}", missing);
        log.info("📊 Base score: {}", calculateMatchScore(requiredSkills, candidateSkills));
        log.info("🎁 Hierarchy bonus: {}", calculateHierarchyBonus(requiredSkills, candidateSkills));
        log.info("🏆 Enhanced score: {}", calculateEnhancedMatchScore(requiredSkills, candidateSkills));
    }
}

