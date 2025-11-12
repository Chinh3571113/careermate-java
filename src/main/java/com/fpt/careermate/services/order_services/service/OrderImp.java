package com.fpt.careermate.services.order_services.service;

import com.fpt.careermate.common.constant.StatusOrder;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.order_services.domain.Invoice;
import com.fpt.careermate.services.order_services.service.dto.response.CandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageCandidateOrderResponse;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.order_services.repository.CandidateOrderRepo;
import com.fpt.careermate.services.order_services.repository.PackageRepo;
import com.fpt.careermate.services.order_services.service.impl.OrderService;
import com.fpt.careermate.services.order_services.service.mapper.OrderMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderImp implements OrderService {

    CandidateOrderRepo candidateOrderRepo;
    PackageRepo packageRepo;
    CandidateRepo candidateRepo;
    OrderMapper orderMapper;
    AuthenticationImp authenticationImp;

    @Transactional
    public void createOrder(String packageName, Candidate currentCandidate) {
        CandidatePackage pkg = packageRepo.findByName(packageName);

        Invoice invoice = new Invoice();
        invoice.setCandidate(currentCandidate);
        invoice.setCandidatePackage(pkg);
        invoice.setAmount(pkg.getPrice());
        invoice.setStatus(StatusOrder.PAID);
        invoice.setCreateAt(LocalDate.now());
        invoice.setActive(true);
        invoice.setStartDate(LocalDate.now());
        invoice.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));

        candidateOrderRepo.save(invoice);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public void cancelOrder(int id) {
        Invoice invoice = candidateOrderRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (invoice.getStatus().equals(StatusOrder.PAID)) {
            invoice.setStatus(StatusOrder.CANCELLED);
            invoice.setCancelledAt(LocalDate.now());
            invoice.setActive(false);
            candidateOrderRepo.save(invoice);
        }
        else {
            throw new AppException(ErrorCode.CANNOT_DELETE_ORDER);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public PageCandidateOrderResponse getOrderList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());

        Page<Invoice> candidateOrders = candidateOrderRepo.findAll(pageable);

        List<CandidateOrderResponse> candidateOrderResponses = new ArrayList<>();
        candidateOrders.getContent().forEach(candidateOrder -> {
            CandidateOrderResponse response = orderMapper.toCandidateOrderResponse(candidateOrder);
            candidateOrderResponses.add(response);
        });

        PageCandidateOrderResponse pageCandidateOrderResponse = orderMapper.toPageCandidateOrderResponse(candidateOrders);
        pageCandidateOrderResponse.setContent(candidateOrderResponses);

        return pageCandidateOrderResponse;
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public MyCandidateOrderResponse myCandidatePackage() {
        Candidate currentCandidate = getCurrentCandidate();

        Optional<Invoice> candidateOrder = candidateOrderRepo.findByCandidate_CandidateIdAndIsActiveTrue(currentCandidate.getCandidateId());

        if(candidateOrder.isEmpty()) throw new AppException(ErrorCode.USING_FREE_PACAKGE);

        return orderMapper.toOrderResponse(candidateOrder.get());
    }
    
    private Candidate getCurrentCandidate(){
        Account currentAccount = authenticationImp.findByEmail();
        Optional<Candidate> candidate = candidateRepo.findByAccount_Id(Integer.valueOf(currentAccount.getId()));
        Candidate currentCandidate = candidate.get();
        return currentCandidate;
    }

    public void updateCandidateOrder(Invoice exstingInvoice, String packageName){
        CandidatePackage pkg = packageRepo.findByName(packageName);

        exstingInvoice.setCandidatePackage(pkg);
        exstingInvoice.setAmount(pkg.getPrice());
        exstingInvoice.setStatus(StatusOrder.PAID);
        exstingInvoice.setCreateAt(LocalDate.now());
        exstingInvoice.setActive(true);
        exstingInvoice.setStartDate(LocalDate.now());
        exstingInvoice.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));
        exstingInvoice.setCancelledAt(null);

        candidateOrderRepo.save(exstingInvoice);
    }

    public CandidatePackage getPackageByName(String packageName){
        return packageRepo.findByName(packageName);
    }

    // Check if candidate has an active order
    public boolean hasActivePackage() {
        Candidate currentCandidate = getCurrentCandidate();
        Optional<Invoice> activeOrder = candidateOrderRepo.findByCandidate_CandidateIdAndIsActiveTrue(currentCandidate.getCandidateId());
        return activeOrder.isPresent();
    }
}
