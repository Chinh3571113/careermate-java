package com.fpt.careermate.services.order_services.service;


import com.fpt.careermate.common.constant.EntitlementCode;
import com.fpt.careermate.common.constant.PackageCode;
import com.fpt.careermate.common.constant.RecruiterEntitlementCode;
import com.fpt.careermate.common.constant.RecruiterPackageCode;
import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.order_services.domain.*;
import com.fpt.careermate.services.order_services.repository.RecruiterEntitlementPackageRepo;
import com.fpt.careermate.services.order_services.repository.RecruiterPackageRepo;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service kiểm tra quyền hạn của Recruiter khi mua gói dịch vụ
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterEntitlementCheckerService {

    RecruiterEntitlementPackageRepo recruiterEntitlementPackageRepo;
    CoachUtil coachUtil;
    RecruiterPackageRepo recruiterPackageRepo;
    JobPostingRepo jobPostingRepo;

    private boolean core(String entitlementCode) {
        // Kiểm tra gói BASIC
        if (checkBasicPackage()) {
            // Nếu là Free package
            log.info("Recruiter is on BASIC");
            RecruiterPackage basicRecruiterPackage = recruiterPackageRepo.findByName(RecruiterPackageCode.BASIC);
            RecruiterEntitlementPackage entitlement = recruiterEntitlementPackageRepo
                    .findByRecruiterPackage_NameAndRecruiterEntitlement_Code(basicRecruiterPackage.getName(), entitlementCode);
            return entitlement != null && entitlement.isEnabled();
        }

        RecruiterPackage currentRecruiterPackage = coachUtil.getCurrentRecruiter().getRecruiterInvoice().getRecruiterPackage();

        // Lấy recruiterEntitlement "entitlementCode"
        RecruiterEntitlementPackage entitlement = recruiterEntitlementPackageRepo
                .findByRecruiterPackage_NameAndRecruiterEntitlement_Code(currentRecruiterPackage.getName(), entitlementCode);

        // Trả kết quả
        return entitlement != null && entitlement.isEnabled();
    }

    // Khi có recruiter mới, kiểm tra recruiterInvoice == null hoặc active == false là Free
    private boolean checkBasicPackage() {
        Recruiter currentRecruiter = coachUtil.getCurrentRecruiter();
        RecruiterInvoice recruiterInvoice = currentRecruiter.getRecruiterInvoice();

        if(recruiterInvoice == null || !recruiterInvoice.isActive()) {
            return true; // recruiter chưa có package active → coi như BASIC
        }

        return false;  // Recruiter đã có package active
    }

    /**
     * Kiểm tra recruiter có quyền dùng tính năng ai matching không?
     */
    public boolean canUseAiMatching() {
        return core(RecruiterEntitlementCode.AI_MATCHING);
    }

    /**
     * Kiểm tra recruiter có thể Post Job thêm không
     * Logic:
     *  - BASIC: được post tối đa 5 job / tháng
     *  - PROFESSIONAL: tối đa 20 job / tháng
     *  - ENTERPRISE: không giới hạn (limit = 0)
     */
    public boolean canPostJob() {
        Recruiter recruiter = coachUtil.getCurrentRecruiter();

        // Lấy tháng hiện tại
        int currentMonth = LocalDate.now().getMonth().getValue();
        // Lấy năm hiện tại
        int currentYear = LocalDate.now().getYear();

        // Đếm số lần post trong tháng này
        int appliedCountThisMonth = jobPostingRepo.countByRecruiterAndMonth(
                recruiter.getId(), currentMonth, currentYear
        );

        // Lấy gói hiện tại
        RecruiterPackage recruiterPackage = checkBasicPackage()
                ? recruiterPackageRepo.findByName(RecruiterPackageCode.BASIC)
                : recruiter.getRecruiterInvoice().getRecruiterPackage();

        // Lấy recruiterEntitlement JOB_POSTING tương ứng với gói đó
        RecruiterEntitlementPackage entitlement = recruiterEntitlementPackageRepo
                .findByRecruiterPackage_NameAndRecruiterEntitlement_Code(
                        recruiterPackage.getName(),
                        RecruiterEntitlementCode.JOB_POSTING
                );

        // Nếu recruiterEntitlement không tồn tại hoặc bị disable → không được post
        if (entitlement == null || !entitlement.isEnabled()) return false;

        // Nếu recruiterEntitlement có limitCount = 0 → không giới hạn post
        Integer limit = entitlement.getLimitValue();
        if (limit == null || limit == 0) return true;

        // Chỉ cho phép post nếu chưa vượt giới hạn trong tháng
        return appliedCountThisMonth < limit;
    }
}
