package com.fpt.careermate.common.seeder;

import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.authentication_services.domain.Role;
import com.fpt.careermate.services.authentication_services.repository.RoleRepo;
import com.fpt.careermate.services.job_services.domain.JobFeedback;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.repository.JobFeedbackRepo;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 🌱 JobFeedbackSeeder
 *
 * Seed dữ liệu cho job postings và job feedback
 * - Kiểm tra nếu chưa có job posting thì tạo 10 job postings
 * - Nếu có rồi thì tạo 10 job feedback với yêu cầu ít nhất 2 candidates thích cùng 1 job
 */
@Component
@Order(2) // Chạy sau CandidateProfileSeeder
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobFeedbackSeeder implements CommandLineRunner {

    JobPostingRepo jobPostingRepo;
    JobFeedbackRepo jobFeedbackRepo;
    CandidateRepo candidateRepo;
    RecruiterRepo recruiterRepo;
    AccountRepo accountRepo;
    RoleRepo roleRepo;
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        long jobPostingCount = jobPostingRepo.count();

        if (jobPostingCount == 0) {
            log.info("🌱 No job postings found. Creating 10 job postings...");
            seedJobPostings();
        } else {
            log.info("ℹ️ Job postings already exist. Count: {}", jobPostingCount);
        }

        // Luôn seed job feedback nếu có candidates và job postings
        long feedbackCount = jobFeedbackRepo.count();
        if (feedbackCount == 0) {
            log.info("🌱 Seeding Job Feedback...");
            seedJobFeedback();
        } else {
            log.info("ℹ️ Job feedback already exists. Count: {}", feedbackCount);
        }
    }

    private void seedJobPostings() {
        // Kiểm tra hoặc tạo recruiter
        Recruiter recruiter = getOrCreateRecruiter();

        // Tạo 10 job postings
        String[] jobTitles = {
            "Senior Java Developer",
            "Frontend ReactJS Developer",
            "Full Stack Engineer",
            "Backend Node.js Developer",
            "DevOps Engineer",
            "Mobile Flutter Developer",
            "Data Analyst",
            "Product Manager",
            "UI/UX Designer",
            "QA Automation Engineer"
        };

        String[] descriptions = {
            "We are looking for an experienced Java Developer with strong Spring Boot knowledge.",
            "Join our team to build modern web applications using ReactJS and TypeScript.",
            "Build scalable applications with both frontend and backend technologies.",
            "Develop robust APIs and microservices using Node.js and Express.",
            "Manage our cloud infrastructure and CI/CD pipelines.",
            "Create beautiful cross-platform mobile apps with Flutter.",
            "Analyze data to drive business decisions and insights.",
            "Lead product development from conception to launch.",
            "Design intuitive and engaging user experiences.",
            "Automate testing processes and ensure quality standards."
        };

        String[] addresses = {
            "District 1, Ho Chi Minh City",
            "District 7, Ho Chi Minh City",
            "Binh Thanh District, Ho Chi Minh City",
            "Tan Binh District, Ho Chi Minh City",
            "District 3, Ho Chi Minh City",
            "Thu Duc City, Ho Chi Minh City",
            "District 2, Ho Chi Minh City",
            "Phu Nhuan District, Ho Chi Minh City",
            "District 10, Ho Chi Minh City",
            "Go Vap District, Ho Chi Minh City"
        };

        int[] yearsOfExperience = {5, 3, 4, 3, 4, 2, 2, 5, 3, 3};
        String[] salaryRanges = {
            "2000-3000 USD",
            "1500-2500 USD",
            "1800-2800 USD",
            "1500-2200 USD",
            "2000-3500 USD",
            "1200-2000 USD",
            "1000-1800 USD",
            "2500-4000 USD",
            "1200-2000 USD",
            "1500-2200 USD"
        };

        for (int i = 0; i < 10; i++) {
            JobPosting jobPosting = JobPosting.builder()
                    .title(jobTitles[i])
                    .description(descriptions[i])
                    .address(addresses[i])
                    .status("ACTIVE")
                    .expirationDate(LocalDate.now().plusMonths(2))
                    .createAt(LocalDate.now())
                    .recruiter(recruiter)
                    .yearsOfExperience(yearsOfExperience[i])
                    .salaryRange(salaryRanges[i])
                    .workModel("Hybrid")
                    .jobPackage("Standard")
                    .build();

            jobPostingRepo.save(jobPosting);
            log.info("✅ Created job posting: {}", jobPosting.getTitle());
        }

        log.info("🎉 Successfully seeded 10 job postings!");
    }

    private void seedJobFeedback() {
        List<Candidate> candidates = candidateRepo.findAll();
        List<JobPosting> jobPostings = jobPostingRepo.findAll();

        if (candidates.isEmpty()) {
            log.warn("⚠️ No candidates found. Skipping job feedback seeding.");
            return;
        }

        if (jobPostings.isEmpty()) {
            log.warn("⚠️ No job postings found. Skipping job feedback seeding.");
            return;
        }

        log.info("📊 Creating job feedback with {} candidates and {} jobs",
                candidates.size(), jobPostings.size());

        // Đảm bảo ít nhất 2 candidates thích cùng 1 job
        // Job đầu tiên sẽ được ít nhất 2 candidates thích
        if (candidates.size() >= 2 && jobPostings.size() >= 1) {
            JobPosting popularJob = jobPostings.get(0);

            // Candidate 1 thích job đầu tiên
            createJobFeedback(candidates.get(0), popularJob, "LIKE", 1.0);
            log.info("✅ Candidate {} liked job: {}",
                    candidates.get(0).getFullName(), popularJob.getTitle());

            // Candidate 2 cũng thích job đầu tiên
            createJobFeedback(candidates.get(1), popularJob, "LIKE", 1.0);
            log.info("✅ Candidate {} liked job: {}",
                    candidates.get(1).getFullName(), popularJob.getTitle());

            // Thêm một số feedback khác
            int feedbackCount = 2;

            // Candidate 1 thích thêm 2 jobs khác
            if (jobPostings.size() >= 3) {
                createJobFeedback(candidates.get(0), jobPostings.get(1), "LIKE", 1.0);
                createJobFeedback(candidates.get(0), jobPostings.get(2), "DISLIKE", 0.0);
                feedbackCount += 2;
            }

            // Candidate 2 xem và thích thêm jobs
            if (jobPostings.size() >= 4 && candidates.size() >= 2) {
                createJobFeedback(candidates.get(1), jobPostings.get(2), "LIKE", 1.0);
                createJobFeedback(candidates.get(1), jobPostings.get(3), "VIEW", 0.5);
                feedbackCount += 2;
            }

            // Nếu có candidate thứ 3, thêm feedback
            if (candidates.size() >= 3) {
                // Candidate 3 cũng thích job đầu tiên (giờ có 3 candidates thích cùng 1 job)
                createJobFeedback(candidates.get(2), popularJob, "LIKE", 1.0);
                log.info("✅ Candidate {} liked job: {}",
                        candidates.get(2).getFullName(), popularJob.getTitle());
                feedbackCount++;

                if (jobPostings.size() >= 5) {
                    createJobFeedback(candidates.get(2), jobPostings.get(3), "LIKE", 1.0);
                    createJobFeedback(candidates.get(2), jobPostings.get(4), "VIEW", 0.5);
                    feedbackCount += 2;
                }
            }

            // Nếu có candidate thứ 4, thêm feedback
            if (candidates.size() >= 4 && jobPostings.size() >= 6) {
                createJobFeedback(candidates.get(3), jobPostings.get(4), "LIKE", 1.0);
                createJobFeedback(candidates.get(3), jobPostings.get(5), "DISLIKE", 0.0);
                feedbackCount += 2;
            }

            log.info("🎉 Successfully seeded {} job feedback entries!", feedbackCount);
            log.info("✨ Job '{}' has at least 2+ candidates who liked it!", popularJob.getTitle());
        }
    }

    private void createJobFeedback(Candidate candidate, JobPosting jobPosting,
                                   String feedbackType, Double score) {
        JobFeedback feedback = new JobFeedback();
        feedback.setCandidate(candidate);
        feedback.setJobPosting(jobPosting);
        feedback.setFeedbackType(feedbackType);
        feedback.setScore(score);
        feedback.setCreateAt(LocalDateTime.now());
        jobFeedbackRepo.save(feedback);
    }

    private Recruiter getOrCreateRecruiter() {
        List<Recruiter> recruiters = recruiterRepo.findAll();

        if (!recruiters.isEmpty()) {
            return recruiters.get(0);
        }

        // Tạo recruiter mới nếu chưa có
        log.info("🌱 Creating recruiter for job postings...");

        Role recruiterRole = roleRepo.findByName("RECRUITER")
                .orElseThrow(() -> new RuntimeException("RECRUITER role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(recruiterRole);

        Account account = Account.builder()
                .username("techcorp_hr")
                .email("hr@techcorp.com")
                .password(passwordEncoder.encode("Password123!"))
                .status("ACTIVE")
                .roles(roles)
                .build();
        accountRepo.save(account);

        Recruiter recruiter = Recruiter.builder()
                .account(account)
                .companyName("TechCorp Solutions")
                .website("https://techcorp.com")
                .logoUrl("https://ui-avatars.com/api/?name=TechCorp&background=0D8ABC&color=fff")
                .about("Leading technology company specializing in software development and IT consulting.")
                .rating(4.5f)
                .companyEmail("contact@techcorp.com")
                .contactPerson("John Smith")
                .phoneNumber("0281234567")
                .companyAddress("123 Tech Street, District 1, Ho Chi Minh City")
                .verificationStatus("APPROVED")
                .build();

        recruiterRepo.save(recruiter);
        log.info("✅ Created recruiter: {}", recruiter.getCompanyName());

        return recruiter;
    }
}

