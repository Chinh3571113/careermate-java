package com.fpt.careermate.services.order_services.service;


import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.order_services.domain.EntitlementPackage;
import com.fpt.careermate.services.order_services.domain.CandidateOrder;
import com.fpt.careermate.services.order_services.repository.EntitlementPackageRepo;
import com.fpt.careermate.services.order_services.repository.PackageRepo;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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


    public boolean core(String entitlementCode) {
        // Kiểm tra gói Free
        if (checkFreePackage()) {
            // Nếu là Free package
            log.info("Candidate is on Free CandidatePackage");
            CandidatePackage freeCandidatePackage = packageRepo.findByName("Free");
            EntitlementPackage entitlement = entitlementPackageRepo
                    .findByCandidatePackage_NameAndEntitlement_Code(freeCandidatePackage.getName(), entitlementCode);
            return entitlement != null && entitlement.isEnabled();
        }

        CandidatePackage currentCandidatePackage = coachUtil.getCurrentCandidate().getCandidateOrder().getCandidatePackage();
        log.info("Current CandidatePackage Name: " + currentCandidatePackage.getName());

        // Lấy entitlement "entitlementCode"
        EntitlementPackage entitlement = entitlementPackageRepo
                .findByCandidatePackage_NameAndEntitlement_Code(currentCandidatePackage.getName(), entitlementCode);

        // Trả kết quả
        return entitlement != null && entitlement.isEnabled();
    }

    // Khi có candidate mới, kiểm tra candidateOrder == null hoặc active == false là Free
    private boolean checkFreePackage() {
        Candidate currentCandidate = coachUtil.getCurrentCandidate();
        CandidateOrder candidateOrder = currentCandidate.getCandidateOrder();

        if(candidateOrder == null || !candidateOrder.isActive()) {
            return true;
        }

        return false;
    }

    /**
     * Kiểm tra candidate có quyền dùng tính năng Job Recommendation không?
     */
    public boolean canUseJobRecommendation() {
        String JOB_RECOMMENDATION = "JOB_RECOMMENDATION";
        return core(JOB_RECOMMENDATION);
    }

    /**
     * Kiểm tra candidate có quyền dùng tính năng Roadmap Recommendation không?
     */
    public boolean canUseRoadmapRecommendation() {
        String AI_ROADMAP = "AI_ROADMAP";
        return core(AI_ROADMAP);
    }
}
