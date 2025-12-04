package com.fpt.careermate.common.seeder;

import com.fpt.careermate.common.constant.EntitlementCode;
import com.fpt.careermate.common.constant.PackageCode;
import com.fpt.careermate.services.order_services.domain.CandidateEntitlement;
import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.order_services.domain.CandidateEntitlementPackage;
import com.fpt.careermate.services.order_services.repository.CandidateEntitlementPackageRepo;
import com.fpt.careermate.services.order_services.repository.CandidateEntitlementRepo;
import com.fpt.careermate.services.order_services.repository.CandidatePackageRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 🌱 DataSeederImp
 *
 * Class này chạy tự động khi Spring Boot khởi động lần đầu tiên.
 * Mục tiêu: Seed dữ liệu mặc định cho bảng candidateEntitlement, package, và mapping giữa chúng.
 * Giúp hệ thống có sẵn các gói và tính năng cơ bản (Free, Plus, Premium).
 */
@Order(1)
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidatePacakgeSeeder implements CommandLineRunner {

    CandidateEntitlementRepo candidateEntitlementRepo;
    CandidatePackageRepo candidatePackageRepo;
    CandidateEntitlementPackageRepo entitlementPackageRepoCandidate;

    @Override
    public void run(String... args) throws Exception {
        seedEntitlements();             // Seed danh sách các quyền lợi (entitlements)
        seedPackages();                 // Seed các gói (Free / Plus / Premium)
        seedEntitlementPackages();      // Gắn quyền lợi cho từng gói
    }

    /**
     * 🧱 Seed bảng candidateEntitlement
     * Đây là bảng mô tả các tính năng trong hệ thống, ví dụ:
     * - CV Builder
     * - Apply Job
     * - AI Analyzer
     * - Recruiter Info Visibility
     * - AI Roadmap
     *
     * Chỉ chạy khi bảng này trống (count() == 0).
     */
    private void seedEntitlements() {
        if (candidateEntitlementRepo.count() == 0) {
            log.info("🌱 Seeding Entitlements...");

            var cvBuilder = new CandidateEntitlement();
            cvBuilder.setName("CV Builder");
            cvBuilder.setCode(EntitlementCode.CV_BUILDER);
            cvBuilder.setUnit("CV");
            cvBuilder.setHasLimit(true);

            var applyJob = new CandidateEntitlement();
            applyJob.setName("Apply Job");
            applyJob.setCode(EntitlementCode.APPLY_JOB);
            applyJob.setUnit("times/month");
            applyJob.setHasLimit(true);

            var aiAnalyzer = new CandidateEntitlement();
            aiAnalyzer.setName("AI Analyzer");
            aiAnalyzer.setCode(EntitlementCode.AI_ANALYZER);
            aiAnalyzer.setUnit("boolean");
            aiAnalyzer.setHasLimit(false);

            var recruiterInfo = new CandidateEntitlement();
            recruiterInfo.setName("Recruiter Info Visibility");
            recruiterInfo.setCode(EntitlementCode.RECRUITER_INFO);
            recruiterInfo.setUnit("boolean");
            recruiterInfo.setHasLimit(true);

            var aiRoadmap = new CandidateEntitlement();
            aiRoadmap.setName("AI Roadmap");
            aiRoadmap.setCode(EntitlementCode.AI_ROADMAP);
            recruiterInfo.setUnit("boolean");
            aiRoadmap.setHasLimit(false);

            var cvDownload = new CandidateEntitlement();
            cvDownload.setName("CV Download (PDF)");
            cvDownload.setCode(EntitlementCode.CV_DOWNLOAD);
            recruiterInfo.setUnit("boolean");
            cvDownload.setHasLimit(false);

            var jobRecommendation = new CandidateEntitlement();
            jobRecommendation.setName("Job Recommendation");
            jobRecommendation.setCode(EntitlementCode.JOB_RECOMMENDATION);
            recruiterInfo.setUnit("boolean");
            jobRecommendation.setHasLimit(false);

            candidateEntitlementRepo.saveAll(List.of(cvBuilder, applyJob, aiAnalyzer, recruiterInfo, aiRoadmap, cvDownload, jobRecommendation));
        }
    }

    /**
     * 💼 Seed bảng package
     * Bao gồm 3 gói cơ bản:
     *  - Free: miễn phí, có giới hạn
     *  - Plus: giá thấp hơn Premium, giới hạn cao hơn
     *  - Premium: đầy đủ tính năng, không giới hạn
     */
    private void seedPackages() {
        if (candidatePackageRepo.count() == 0) {
            log.info("🌱 Seeding Packages...");

            var free = new CandidatePackage();
            free.setName(PackageCode.FREE);
            free.setPrice(0L);
            free.setDurationDays(0);
            free.setPriority(3);
            free.setCreateAt(LocalDateTime.now());

            var plus = new CandidatePackage();
            plus.setName(PackageCode.PLUS);
            plus.setPrice(99000L);
            plus.setDurationDays(30);
            plus.setPriority(2);
            plus.setCreateAt(LocalDateTime.now());

            var premium = new CandidatePackage();
            premium.setName(PackageCode.PREMIUM);
            premium.setPrice(199000L);
            premium.setDurationDays(30);
            premium.setPriority(1);
            premium.setCreateAt(LocalDateTime.now());

            candidatePackageRepo.saveAll(List.of(free, plus, premium));
        }
    }

    /**
     * 🔗 Seed bảng mapping giữa CandidateEntitlement và CandidatePackage
     * - Gắn các quyền và giới hạn cho từng gói
     * - Ví dụ:
     *   + Free chỉ tạo 1 CV, apply 5 lần/tháng
     *   + Plus tạo 3 CV, apply 20 lần/tháng
     *   + Premium không giới hạn
     */
    private void seedEntitlementPackages() {
        LocalDateTime now = LocalDateTime.now();
        if (entitlementPackageRepoCandidate.count() == 0) {
            log.info("🌱 Seeding CandidateEntitlement-CandidatePackage Mappings...");

            var free = candidatePackageRepo.findByName(PackageCode.FREE);
            var plus = candidatePackageRepo.findByName(PackageCode.PLUS);
            var premium = candidatePackageRepo.findByName(PackageCode.PREMIUM);

            var cvBuilder = candidateEntitlementRepo.findByCode(EntitlementCode.CV_BUILDER);
            var applyJob = candidateEntitlementRepo.findByCode(EntitlementCode.APPLY_JOB);
            var aiAnalyzer = candidateEntitlementRepo.findByCode(EntitlementCode.AI_ANALYZER);
            var recruiterInfo = candidateEntitlementRepo.findByCode(EntitlementCode.RECRUITER_INFO);
            var aiRoadmap = candidateEntitlementRepo.findByCode(EntitlementCode.AI_ROADMAP);
            var cvDownload = candidateEntitlementRepo.findByCode(EntitlementCode.CV_DOWNLOAD);
            var jobRecommendation = candidateEntitlementRepo.findByCode(EntitlementCode.JOB_RECOMMENDATION);

            // === Free CandidatePackage ===
            entitlementPackageRepoCandidate.saveAll(List.of(
                    new CandidateEntitlementPackage(true, 1, now, cvBuilder, free),
                    new CandidateEntitlementPackage(true, 5, now, applyJob, free),
                    new CandidateEntitlementPackage(false, 0, now, aiAnalyzer, free),
                    new CandidateEntitlementPackage(false, 0, now, recruiterInfo, free),
                    new CandidateEntitlementPackage(false, 0, now, aiRoadmap, free),
                    new CandidateEntitlementPackage(false, 0, now, cvDownload, free),
                    new CandidateEntitlementPackage(false, 0, now, jobRecommendation, free)
            ));

            // === Plus CandidatePackage ===
            entitlementPackageRepoCandidate.saveAll(List.of(
                    new CandidateEntitlementPackage(true, 3, now, cvBuilder, plus),
                    new CandidateEntitlementPackage(true, 20, now, applyJob, plus),
                    new CandidateEntitlementPackage(true, 0, now, aiAnalyzer, plus),
                    new CandidateEntitlementPackage(true, 0, now, recruiterInfo, plus),
                    new CandidateEntitlementPackage(false, 0, now, aiRoadmap, plus),
                    new CandidateEntitlementPackage(true, 0, now, cvDownload, plus),
                    new CandidateEntitlementPackage(true, 0, now, jobRecommendation, plus)
            ));

            // === Premium CandidatePackage ===
            entitlementPackageRepoCandidate.saveAll(List.of(
                    new CandidateEntitlementPackage(true, 0, now, cvBuilder, premium),
                    new CandidateEntitlementPackage(true, 0, now, applyJob, premium),
                    new CandidateEntitlementPackage(true, 0, now, aiAnalyzer, premium),
                    new CandidateEntitlementPackage(true, 0, now, recruiterInfo, premium),
                    new CandidateEntitlementPackage(true, 0, now, aiRoadmap, premium),
                    new CandidateEntitlementPackage(true, 0, now, cvDownload, premium),
                    new CandidateEntitlementPackage(true, 0, now, jobRecommendation, premium)
            ));
        }
    }
}
