package com.fpt.careermate.services.order_services.service;

import com.fpt.careermate.common.constant.EntitlementCode;
import com.fpt.careermate.common.constant.PackageCode;
import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.order_services.domain.Entitlement;
import com.fpt.careermate.services.order_services.domain.EntitlementPackage;
import com.fpt.careermate.services.order_services.repository.EntitlementPackageRepo;
import com.fpt.careermate.services.order_services.repository.EntitlementRepo;
import com.fpt.careermate.services.order_services.repository.PackageRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 🌱 DataSeederImp
 *
 * Class này chạy tự động khi Spring Boot khởi động lần đầu tiên.
 * Mục tiêu: Seed dữ liệu mặc định cho bảng entitlement, package, và mapping giữa chúng.
 * Giúp hệ thống có sẵn các gói và tính năng cơ bản (Free, Plus, Premium).
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DataSeederImp implements CommandLineRunner {

    EntitlementRepo entitlementRepo;
    PackageRepo packageRepo;
    EntitlementPackageRepo entitlementpackageRepo;

    @Override
    public void run(String... args) throws Exception {
        seedEntitlements();             // Seed danh sách các quyền lợi (entitlements)
        seedPackages();                 // Seed các gói (Free / Plus / Premium)
        seedEntitlementPackages();      // Gắn quyền lợi cho từng gói
    }

    /**
     * 🧱 Seed bảng entitlement
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
        if (entitlementRepo.count() == 0) {
            log.info("🌱 Seeding Entitlements...");

            var cvBuilder = new Entitlement();
            cvBuilder.setName("CV Builder");
            cvBuilder.setCode(EntitlementCode.CV_BUILDER);
            cvBuilder.setUnit("CV");
            cvBuilder.setHasLimit(true);

            var applyJob = new Entitlement();
            applyJob.setName("Apply Job");
            applyJob.setCode(EntitlementCode.APPLY_JOB);
            applyJob.setUnit("times/month");
            applyJob.setHasLimit(true);

            var aiAnalyzer = new Entitlement();
            aiAnalyzer.setName("AI Analyzer");
            aiAnalyzer.setCode(EntitlementCode.AI_ANALYZER);
            aiAnalyzer.setUnit("boolean");
            aiAnalyzer.setHasLimit(false);

            var recruiterInfo = new Entitlement();
            recruiterInfo.setName("Recruiter Info Visibility");
            recruiterInfo.setCode(EntitlementCode.RECRUITER_INFO);
            recruiterInfo.setUnit("boolean");
            recruiterInfo.setHasLimit(true);

            var aiRoadmap = new Entitlement();
            aiRoadmap.setName("AI Roadmap");
            aiRoadmap.setCode(EntitlementCode.AI_ROADMAP);
            recruiterInfo.setUnit("boolean");
            aiRoadmap.setHasLimit(false);

            var cvDownload = new Entitlement();
            cvDownload.setName("CV Download (PDF)");
            cvDownload.setCode(EntitlementCode.CV_DOWNLOAD);
            recruiterInfo.setUnit("boolean");
            cvDownload.setHasLimit(false);

            var jobRecommendation = new Entitlement();
            jobRecommendation.setName("Job Recommendation");
            jobRecommendation.setCode(EntitlementCode.JOB_RECOMMENDATION);
            recruiterInfo.setUnit("boolean");
            jobRecommendation.setHasLimit(false);

            entitlementRepo.saveAll(List.of(cvBuilder, applyJob, aiAnalyzer, recruiterInfo, aiRoadmap, cvDownload, jobRecommendation));
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
        if (packageRepo.count() == 0) {
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

            packageRepo.saveAll(List.of(free, plus, premium));
        }
    }

    /**
     * 🔗 Seed bảng mapping giữa Entitlement và CandidatePackage
     * - Gắn các quyền và giới hạn cho từng gói
     * - Ví dụ:
     *   + Free chỉ tạo 1 CV, apply 5 lần/tháng
     *   + Plus tạo 3 CV, apply 20 lần/tháng
     *   + Premium không giới hạn
     */
    private void seedEntitlementPackages() {
        LocalDateTime now = LocalDateTime.now();
        if (entitlementpackageRepo.count() == 0) {
            log.info("🌱 Seeding Entitlement-CandidatePackage Mappings...");

            var free = packageRepo.findByName(PackageCode.FREE);
            var plus = packageRepo.findByName(PackageCode.PLUS);
            var premium = packageRepo.findByName(PackageCode.PREMIUM);

            var cvBuilder = entitlementRepo.findByCode(EntitlementCode.CV_BUILDER);
            var applyJob = entitlementRepo.findByCode(EntitlementCode.APPLY_JOB);
            var aiAnalyzer = entitlementRepo.findByCode(EntitlementCode.AI_ANALYZER);
            var recruiterInfo = entitlementRepo.findByCode(EntitlementCode.RECRUITER_INFO);
            var aiRoadmap = entitlementRepo.findByCode(EntitlementCode.AI_ROADMAP);
            var cvDownload = entitlementRepo.findByCode(EntitlementCode.CV_DOWNLOAD);
            var jobRecommendation = entitlementRepo.findByCode(EntitlementCode.JOB_RECOMMENDATION);

            // === Free CandidatePackage ===
            entitlementpackageRepo.saveAll(List.of(
                    new EntitlementPackage(true, 1, now, cvBuilder, free),
                    new EntitlementPackage(true, 5, now, applyJob, free),
                    new EntitlementPackage(false, 0, now, aiAnalyzer, free),
                    new EntitlementPackage(false, 0, now, recruiterInfo, free),
                    new EntitlementPackage(false, 0, now, aiRoadmap, free),
                    new EntitlementPackage(false, 0, now, cvDownload, free),
                    new EntitlementPackage(false, 0, now, jobRecommendation, free)
                    ));

            // === Plus CandidatePackage ===
            entitlementpackageRepo.saveAll(List.of(
                    new EntitlementPackage(true, 3, now, cvBuilder, plus),
                    new EntitlementPackage(true, 20, now, applyJob, plus),
                    new EntitlementPackage(true, 0, now, aiAnalyzer, plus),
                    new EntitlementPackage(true, 0, now, recruiterInfo, plus),
                    new EntitlementPackage(false, 0, now, aiRoadmap, plus),
                    new EntitlementPackage(true, 0, now, cvDownload, plus),
                    new EntitlementPackage(true, 0, now, jobRecommendation, plus)
                    ));

            // === Premium CandidatePackage ===
            entitlementpackageRepo.saveAll(List.of(
                    new EntitlementPackage(true, 0, now, cvBuilder, premium),
                    new EntitlementPackage(true, 0, now, applyJob, premium),
                    new EntitlementPackage(true, 0, now, aiAnalyzer, premium),
                    new EntitlementPackage(true, 0, now, recruiterInfo, premium),
                    new EntitlementPackage(true, 0, now, aiRoadmap, premium),
                    new EntitlementPackage(true, 0, now, cvDownload, premium),
                    new EntitlementPackage(true, 0, now, jobRecommendation, premium)
                    ));
        }
    }
}