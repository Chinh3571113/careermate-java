package com.fpt.careermate.common.seeder;

import com.fpt.careermate.services.job_services.domain.JdSkill;
import com.fpt.careermate.services.job_services.repository.JdSkillRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 🌱 JdSkillSeeder
 *
 * Seed dữ liệu cho jdskill
 * - Kiểm tra nếu chưa có jdskill thì tạo thêm, nếu có rồi thì không làm gì
 */
@Component
@Order(1)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JdSkillSeeder implements CommandLineRunner {

    JdSkillRepo jdSkillRepo;

    @Override
    public void run(String... args) throws Exception {
        List<String> skillsToSeed = Arrays.asList(
            // Frontend Skills
            "HTML/CSS",
            "JavaScript",
            "React",
            "Vue",
            "Angular",
            "Responsive Design",
            "TypeScript",
            "Redux",
            "Zustand",
            "Vuex",
            "RESTful API",
            "TailwindCSS",
            "Material UI",

            // Backend Skills
            "Java",
            "Spring Boot",
            "Node.js",
            "Express",
            ".NET",
            "SQL",
            "MySQL",
            "PostgreSQL",
            "Microservices basics",

            // DevOps & Infrastructure
            "Redis",
            "Docker",
            "Kafka",
            "RabbitMQ",
            "CI/CD",

            // Mobile Development - Android
            "Kotlin",
            "Android SDK",
            "Jetpack Compose",
            "REST API",

            // Mobile Development - iOS
            "Swift",
            "UIKit",
            "SwiftUI",
            "Xcode",

            // Mobile Development - Cross Platform
            "React Native Core APIs",
            "API integration",

            // Testing
            "Test case design",
            "Manual testing",
            "Bug tracking tools",
            "Jira",
            "API testing",
            "Postman",

            // Data Engineering
            "Python",
            "ETL",
            "Data Pipeline",
            "Cloud",
            "AWS",
            "GCP",
            "Azure",

            // Data Analysis
            "Excel",
            "Google Sheets",
            "Power BI",
            "Tableau",
            "Data visualization",

            // Machine Learning & AI
            "Machine Learning",
            "Sklearn",
            "Deep Learning",
            "TensorFlow",
            "PyTorch",
            "Data preprocessing",

            // System Administration
            "Linux",
            "GitHub Actions",
            "GitLab CI",
            "Jenkins"
        );

        int addedCount = 0;
        int existingCount = 0;

        for (String skillName : skillsToSeed) {
            Optional<JdSkill> existingSkill = jdSkillRepo.findSkillByName(skillName);

            if (existingSkill.isEmpty()) {
                JdSkill newSkill = JdSkill.builder()
                    .name(skillName)
                    .build();
                jdSkillRepo.save(newSkill);
                addedCount++;
            } else {
                existingCount++;
            }
        }

        log.info("JdSkillSeeder completed: Added {} new skills, {} skills already existed",
                addedCount, existingCount);
    }

}
