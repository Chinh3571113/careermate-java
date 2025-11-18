package com.fpt.careermate.common.seeder;

import com.fpt.careermate.common.constant.RecruiterEntitlementCode;
import com.fpt.careermate.common.constant.RecruiterPackageCode;
import com.fpt.careermate.services.order_services.domain.RecruiterEntitlement;
import com.fpt.careermate.services.order_services.domain.RecruiterEntitlementPackage;
import com.fpt.careermate.services.order_services.domain.RecruiterPackage;
import com.fpt.careermate.services.order_services.repository.RecruiterEntitlementRepo;
import com.fpt.careermate.services.order_services.repository.RecruiterEntitlementPackageRepo;
import com.fpt.careermate.services.order_services.repository.RecruiterPackageRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 🌱 RecruiterPackageSeeder
 *
 * Class này chạy tự động khi Spring Boot khởi động lần đầu tiên.
 * Mục tiêu: Seed dữ liệu mặc định cho bảng recruiter_entitlement,
 * recruiter_package, và mapping giữa chúng.
 * Giúp hệ thống có sẵn các gói dành cho nhà tuyển dụng (Basic, Professional, Enterprise).
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterPackageSeeder implements CommandLineRunner {

    RecruiterEntitlementRepo recruiterEntitlementRepo;
    RecruiterPackageRepo recruiterPackageRepo;
    RecruiterEntitlementPackageRepo recruiterEntitlementPackageRepo;

    @Override
    public void run(String... args) throws Exception {
        seedRecruiterEntitlements();
        seedRecruiterPackages();
        seedRecruiterEntitlementPackages();
    }

    /**
     * 🧱 Seed bảng recruiter_entitlement
     * Đây là bảng mô tả các tính năng dành cho nhà tuyển dụng, ví dụ:
     * - Job Posting (số lượng tin tuyển dụng)
     * - CV View (xem CV)
     * - AI Matching (gợi ý ứng viên phù hợp)
     */
    private void seedRecruiterEntitlements() {
        if (recruiterEntitlementRepo.findByCode(RecruiterEntitlementCode.JOB_POSTING) == null) {
            log.info("🌱 Seeding Recruiter Entitlements...");

            var jobPosting = RecruiterEntitlement.builder()
                    .name("Job Posting")
                    .code(RecruiterEntitlementCode.JOB_POSTING)
                    .unit("posts/month")
                    .hasLimit(true)
                    .build();

            var cvView = RecruiterEntitlement.builder()
                    .name("CV View")
                    .code(RecruiterEntitlementCode.CV_VIEW)
                    .unit("views/month")
                    .hasLimit(true)
                    .build();

            var aiMatching = RecruiterEntitlement.builder()
                    .name("AI Matching")
                    .code(RecruiterEntitlementCode.AI_MATCHING)
                    .unit("feature")
                    .hasLimit(false)
                    .build();

            recruiterEntitlementRepo.saveAll(List.of(
                    jobPosting,
                    cvView,
                    aiMatching
            ));
        }
    }

    /**
     * 💼 Seed bảng recruiter_package
     * Bao gồm 3 gói dành cho nhà tuyển dụng:
     *  - Basic: gói cơ bản, giới hạn số lượng tin và tính năng
     *  - Professional: gói chuyên nghiệp, tính năng mở rộng
     *  - Enterprise: gói doanh nghiệp, đầy đủ tính năng không giới hạn
     */
    private void seedRecruiterPackages() {
        if (recruiterPackageRepo.count() == 0) {
            log.info("🌱 Seeding Recruiter Packages...");

            var basic = RecruiterPackage.builder()
                    .name(RecruiterPackageCode.BASIC)
                    .price(299000L)
                    .durationDays(30)
                    .priority(3)
                    .createAt(LocalDateTime.now())
                    .build();

            var professional = RecruiterPackage.builder()
                    .name(RecruiterPackageCode.PROFESSIONAL)
                    .price(599000L)
                    .durationDays(30)
                    .priority(2)
                    .createAt(LocalDateTime.now())
                    .build();

            var enterprise = RecruiterPackage.builder()
                    .name(RecruiterPackageCode.ENTERPRISE)
                    .price(999000L)
                    .durationDays(30)
                    .priority(1)
                    .createAt(LocalDateTime.now())
                    .build();

            recruiterPackageRepo.saveAll(List.of(basic, professional, enterprise));
        }
    }

    /**
     * 🔗 Seed bảng mapping giữa RecruiterEntitlement và RecruiterPackage
     * - Gắn các quyền và giới hạn cho từng gói
     * - Ví dụ:
     *   + Basic: 5 tin/tháng, 20 lượt tìm kiếm, 50 lượt xem CV
     *   + Professional: 20 tin/tháng, 100 lượt tìm kiếm, 200 lượt xem CV, có AI
     *   + Enterprise: không giới hạn, full tính năng
     */
    private void seedRecruiterEntitlementPackages() {
        LocalDateTime now = LocalDateTime.now();
        if (recruiterEntitlementPackageRepo.count() == 0) {
            log.info("🌱 Seeding Recruiter Entitlement-Package Mappings...");

            var basic = recruiterPackageRepo.findByName(RecruiterPackageCode.BASIC);
            var professional = recruiterPackageRepo.findByName(RecruiterPackageCode.PROFESSIONAL);
            var enterprise = recruiterPackageRepo.findByName(RecruiterPackageCode.ENTERPRISE);

            var jobPosting = recruiterEntitlementRepo.findByCode(RecruiterEntitlementCode.JOB_POSTING);
            var cvView = recruiterEntitlementRepo.findByCode(RecruiterEntitlementCode.CV_VIEW);
            var aiMatching = recruiterEntitlementRepo.findByCode(RecruiterEntitlementCode.AI_MATCHING);

            // === Basic Package ===
            recruiterEntitlementPackageRepo.saveAll(List.of(
                    new RecruiterEntitlementPackage(true, 5, now, jobPosting, basic),
                    new RecruiterEntitlementPackage(false, 0, now, cvView, basic),
                    new RecruiterEntitlementPackage(false, 0, now, aiMatching, basic)
            ));

            // === Professional Package ===
            recruiterEntitlementPackageRepo.saveAll(List.of(
                    new RecruiterEntitlementPackage(true, 20, now, jobPosting, professional),
                    new RecruiterEntitlementPackage(true, 0, now, cvView, professional),
                    new RecruiterEntitlementPackage(true, 0, now, aiMatching, professional)
            ));

            // === Enterprise Package ===
            recruiterEntitlementPackageRepo.saveAll(List.of(
                    new RecruiterEntitlementPackage(true, 0, now, jobPosting, enterprise),
                    new RecruiterEntitlementPackage(true, 0, now, cvView, enterprise),
                    new RecruiterEntitlementPackage(true, 0, now, aiMatching, enterprise)
            ));
        }
    }
}
