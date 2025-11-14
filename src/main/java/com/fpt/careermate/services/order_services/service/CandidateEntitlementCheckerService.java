package com.fpt.careermate.services.order_services.service;


import com.fpt.careermate.common.constant.EntitlementCode;
import com.fpt.careermate.common.constant.PackageCode;
import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.order_services.domain.EntitlementPackage;
import com.fpt.careermate.services.order_services.domain.Invoice;
import com.fpt.careermate.services.order_services.repository.EntitlementPackageRepo;
import com.fpt.careermate.services.order_services.repository.PackageRepo;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

/**
 * Service kiểm tra quyền hạn của Candidate khi mua gói dịch vụ
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateEntitlementCheckerService {

    EntitlementPackageRepo entitlementPackageRepo;
    CoachUtil coachUtil;
    PackageRepo packageRepo;
    JobApplyRepo jobApplyRepo;


    public boolean core(String entitlementCode) {
        // Kiểm tra gói Free
        if (checkFreePackage()) {
            // Nếu là Free package
            log.info("Candidate is on Free CandidatePackage");
            CandidatePackage freeCandidatePackage = packageRepo.findByName(PackageCode.FREE);
            EntitlementPackage entitlement = entitlementPackageRepo
                    .findByCandidatePackage_NameAndEntitlement_Code(freeCandidatePackage.getName(), entitlementCode);
            return entitlement != null && entitlement.isEnabled();
        }

        CandidatePackage currentCandidatePackage = coachUtil.getCurrentCandidate().getInvoice().getCandidatePackage();

        // Lấy entitlement "entitlementCode"
        EntitlementPackage entitlement = entitlementPackageRepo
                .findByCandidatePackage_NameAndEntitlement_Code(currentCandidatePackage.getName(), entitlementCode);

        // Trả kết quả
        return entitlement != null && entitlement.isEnabled();
    }

    // Khi có candidate mới, kiểm tra invoice == null hoặc active == false là Free
    private boolean checkFreePackage() {
        Candidate currentCandidate = coachUtil.getCurrentCandidate();
        Invoice invoice = currentCandidate.getInvoice();

        if(invoice == null || !invoice.isActive()) {
            return true; // candidate chưa có package active → coi như Free
        }

        return false;  // candidate đã có package active
    }

    /**
     * Kiểm tra candidate có quyền dùng tính năng Job Recommendation không?
     */
    public boolean canUseJobRecommendation() {
        return core(EntitlementCode.JOB_RECOMMENDATION);
    }

    /**
     * Kiểm tra candidate có quyền dùng tính năng Roadmap Recommendation không?
     */
    public boolean canUseRoadmapRecommendation() {
        return core(EntitlementCode.AI_ROADMAP);
    }

    /**
     * Kiểm tra candidate có quyền dùng tính năng AI Analyzer không?
     */

    public boolean canUseAIAnalyzer() {
        return core(EntitlementCode.AI_ANALYZER);
    }

    /**
     * 🧱 Kiểm tra candidate có thể tạo thêm CV mới hay không.
     * Logic:
     *  - Free: tạo tối đa 1 CV
     *  - Plus: tối đa 3 CV
     *  - Premium: không giới hạn (limit = 0)
     */
    public boolean canCreateNewCV() {
        Candidate candidate = coachUtil.getCurrentCandidate();

        // Đếm số lượng CV hiện có của candidate
        int currentCvCount = candidate.getResumes().size();

        // Lấy gói hiện tại (Free nếu không có invoice hoạt động)
        CandidatePackage candidatePackage = checkFreePackage()
                ? packageRepo.findByName(PackageCode.FREE)
                : candidate.getInvoice().getCandidatePackage();

        // Lấy entitlement CV_BUILDER tương ứng với gói đó
        EntitlementPackage entitlement = entitlementPackageRepo
                .findByCandidatePackage_NameAndEntitlement_Code(
                        candidatePackage.getName(),
                        EntitlementCode.CV_BUILDER
                );

        // Nếu entitlement không tồn tại hoặc bị disable → không được tạo
        if (entitlement == null || !entitlement.isEnabled()) return false;

        // Nếu limit = 0 → nghĩa là không giới hạn
        Integer limit = entitlement.getLimitValue();
        if (limit == null || limit == 0) return true;

        // Chỉ cho phép tạo mới nếu chưa vượt giới hạn
        return currentCvCount < limit;
    }

    /**
     * Kiểm tra candidate có thể Apply Job thêm không
     * Logic:
     *  - Free: được apply tối đa 5 job / tháng
     *  - Plus: tối đa 20 job / tháng
     *  - Premium: không giới hạn (limit = 0)
     */
    public boolean canApplyJob() {
        Candidate candidate = coachUtil.getCurrentCandidate();

        // Lấy tháng hiện tại
        int currentMonth = LocalDate.now().getMonth().getValue();
        // Lấy năm hiện tại
        int currentYear = LocalDate.now().getYear();

        // Đếm số lần apply trong tháng này
        int appliedCountThisMonth = jobApplyRepo.countByCandidateAndMonth(candidate.getCandidateId(), currentMonth, currentYear);

        // Lấy gói hiện tại
        CandidatePackage candidatePackage = checkFreePackage()
                ? packageRepo.findByName(PackageCode.FREE)
                : candidate.getInvoice().getCandidatePackage();

        // Lấy entitlement APPLY_JOB tương ứng với gói đó
        EntitlementPackage entitlement = entitlementPackageRepo
                .findByCandidatePackage_NameAndEntitlement_Code(
                        candidatePackage.getName(),
                        EntitlementCode.APPLY_JOB
                );

        // Nếu entitlement không tồn tại hoặc bị disable → không được apply
        if (entitlement == null || !entitlement.isEnabled()) return false;

        // Nếu entitlement có limitCount = 0 → không giới hạn apply
        Integer limit = entitlement.getLimitValue();
        if (limit == null || limit == 0) return true;

        // Chỉ cho phép apply nếu chưa vượt giới hạn trong tháng
        return appliedCountThisMonth < limit;
    }

    /**
     * Kiểm tra candidate có quyền dùng tính năng xem recruiter info không?
     */
    public boolean canReviewRecruiterInfo() {
        return core(EntitlementCode.RECRUITER_INFO);
    }

    /**
     * Kiểm tra candidate có quyền dùng tính năng xem download CV không?
     */
    public boolean canDownloadCV() {
        return core(EntitlementCode.CV_DOWNLOAD);
    }
}
