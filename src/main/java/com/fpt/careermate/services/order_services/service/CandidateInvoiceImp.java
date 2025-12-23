package com.fpt.careermate.services.order_services.service;

import com.fpt.careermate.common.constant.StatusInvoice;
import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.order_services.domain.CandidateInvoice;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateInvoiceResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyInvoiceListItemResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageMyInvoiceListResponse;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.order_services.repository.CandidateInvoiceRepo;
import com.fpt.careermate.services.order_services.repository.CandidatePackageRepo;
import com.fpt.careermate.services.order_services.service.impl.CandidateInvoiceService;
import com.fpt.careermate.services.order_services.service.mapper.CandidateInvoiceMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidateInvoiceImp implements CandidateInvoiceService {

    CandidateInvoiceRepo candidateInvoiceRepo;
    CandidatePackageRepo candidatePackageRepo;
    CandidateRepo candidateRepo;
    CandidateInvoiceMapper candidateInvoiceMapper;
    AuthenticationImp authenticationImp;
    CoachUtil coachUtil;

//    @Transactional
    public void createInvoice(String packageName, Candidate currentCandidate) {
        CandidatePackage pkg = candidatePackageRepo.findByName(packageName);

        CandidateInvoice candidateInvoice = new CandidateInvoice();
        candidateInvoice.setCandidate(currentCandidate);
        candidateInvoice.setCandidatePackage(pkg);
        candidateInvoice.setAmount(pkg.getPrice());
        candidateInvoice.setStatus(StatusInvoice.PAID);
        candidateInvoice.setActive(true);
        candidateInvoice.setStartDate(LocalDate.now());
        candidateInvoice.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));

        candidateInvoiceRepo.save(candidateInvoice);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public void cancelMyInvoice() {
        Candidate currentCandidate = getCurrentCandidate();
        CandidateInvoice candidateInvoice =
                candidateInvoiceRepo.findByCandidate_CandidateIdAndIsActiveTrue(currentCandidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_INVOICE_NOT_FOUND));

        if (candidateInvoice.isActive()) {
            candidateInvoice.setStatus(StatusInvoice.CANCELLED);
            candidateInvoice.setCancelledAt(LocalDate.now());
            candidateInvoice.setActive(false);
            candidateInvoiceRepo.save(candidateInvoice);
        }
        else {
            throw new AppException(ErrorCode.CANNOT_DELETE_ORDER);
        }
    }

    private Candidate getCurrentCandidate(){
        Account currentAccount = authenticationImp.findByEmail();
        Optional<Candidate> candidate = candidateRepo.findByAccount_Id(Integer.valueOf(currentAccount.getId()));
        Candidate currentCandidate = candidate.get();
        return currentCandidate;
    }

    public void updateCandidateOrder(CandidateInvoice exstingCandidateInvoice, String packageName){
        CandidatePackage pkg = candidatePackageRepo.findByName(packageName);

        exstingCandidateInvoice.setCandidatePackage(pkg);
        exstingCandidateInvoice.setAmount(pkg.getPrice());
        exstingCandidateInvoice.setStatus(StatusInvoice.PAID);
        exstingCandidateInvoice.setActive(true);
        exstingCandidateInvoice.setStartDate(LocalDate.now());
        exstingCandidateInvoice.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));
        exstingCandidateInvoice.setCancelledAt(null);

        candidateInvoiceRepo.save(exstingCandidateInvoice);
    }

    public CandidatePackage getPackageByName(String packageName){
        return candidatePackageRepo.findByName(packageName);
    }

    // Check if candidate has an active order
    public boolean hasActivePackage() {
        Candidate currentCandidate = getCurrentCandidate();
        Optional<CandidateInvoice> activeOrder = candidateInvoiceRepo.findByCandidate_CandidateIdAndIsActiveTrue(currentCandidate.getCandidateId());
        return activeOrder.isPresent();
    }

    // Get my active invoice by candidate id
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public MyCandidateInvoiceResponse getMyActiveInvoice() {
        Candidate currentCandidate = coachUtil.getCurrentCandidate();
        Optional<CandidateInvoice> exsting =
                candidateInvoiceRepo.findByCandidate_CandidateIdAndIsActiveTrue(
                        currentCandidate.getCandidateId()
                );

        // Return null instead of throwing exception when no active invoice
        // This allows new candidates or those without active subscriptions to access the app
        if(exsting.isEmpty()) {
            log.info("No active invoice found for candidate ID: {}", currentCandidate.getCandidateId());
            return null;
        }

        return candidateInvoiceMapper.toMyCandidateInvoiceResponse(exsting.get());
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public PageMyInvoiceListResponse getMyInvoiceHistory(int page, int size) {
        Candidate currentCandidate = coachUtil.getCurrentCandidate();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<CandidateInvoice> invoicePage = candidateInvoiceRepo.findByCandidate_CandidateIdOrderByIdDesc(
                currentCandidate.getCandidateId(),
                pageable
        );

        List<MyInvoiceListItemResponse> content = invoicePage.getContent().stream()
                .map(candidateInvoiceMapper::toMyInvoiceListItemResponse)
            .collect(Collectors.toList());

        return PageMyInvoiceListResponse.builder()
                .content(content)
                .number(invoicePage.getNumber())
                .size(invoicePage.getSize())
                .totalElements(invoicePage.getTotalElements())
                .totalPages(invoicePage.getTotalPages())
                .first(invoicePage.isFirst())
                .last(invoicePage.isLast())
                .build();
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public MyInvoiceListItemResponse getMyInvoiceById(int invoiceId) {
        Candidate currentCandidate = coachUtil.getCurrentCandidate();
        CandidateInvoice invoice = candidateInvoiceRepo.findByIdAndCandidate_CandidateId(
                        invoiceId,
                        currentCandidate.getCandidateId()
                )
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_INVOICE_NOT_FOUND));

        return candidateInvoiceMapper.toMyInvoiceListItemResponse(invoice);
    }
}
