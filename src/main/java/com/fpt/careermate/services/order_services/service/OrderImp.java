package com.fpt.careermate.services.order_services.service;

import com.fpt.careermate.common.constant.StatusOrder;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.order_services.domain.CandidateOrder;
import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.order_services.repository.OrderRepo;
import com.fpt.careermate.services.order_services.repository.PackageRepo;
import com.fpt.careermate.services.order_services.service.dto.response.OrderResponse;
import com.fpt.careermate.services.order_services.service.impl.OrderService;
import com.fpt.careermate.services.order_services.service.mapper.OrderMapper;
import com.fpt.careermate.common.util.PaymentUtil;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderImp implements OrderService {

    OrderRepo orderRepo;
    PackageRepo packageRepo;
    CandidateRepo candidateRepo;
    OrderMapper orderMapper;
    AuthenticationImp authenticationImp;
    PaymentUtil paymentUtil;

    @Transactional
    public void createOrder(String packageName, Candidate currentCandidate) {
        CandidatePackage pkg = packageRepo.findByName(packageName);

        CandidateOrder candidateOrder = new CandidateOrder();
        candidateOrder.setCandidate(currentCandidate);
        candidateOrder.setCandidatePackage(pkg);
        candidateOrder.setAmount(pkg.getPrice());
        candidateOrder.setStatus(StatusOrder.PAID);
        candidateOrder.setCreateAt(LocalDate.now());
        candidateOrder.setActive(true);
        candidateOrder.setStartDate(LocalDate.now());
        candidateOrder.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));

        orderRepo.save(candidateOrder);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public void deleteOrder(int id) {
        CandidateOrder candidateOrder = orderRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!candidateOrder.getStatus().equals(StatusOrder.PENDING)) {
            throw new AppException(ErrorCode.CANNOT_DELETE_ORDER);
        }

        candidateOrder.setStatus(StatusOrder.CANCELLED);
        candidateOrder.setCancelledAt(LocalDate.now());
        orderRepo.save(candidateOrder);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public String checkOrderStatus(int id) {
        CandidateOrder candidateOrder = orderRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        return candidateOrder.getStatus();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<OrderResponse> getOrderList() {
        return orderMapper.toOrderResponseList(orderRepo.findAll());
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public List<OrderResponse> myOrderList() {
        Candidate currentCandidate = getCurrentCandidate();

        List<CandidateOrder> candidateOrders = orderRepo.findByCandidate_CandidateId(currentCandidate.getCandidateId());
        return orderMapper.toOrderResponseList(candidateOrders);
    }
    
    private Candidate getCurrentCandidate(){
        Account currentAccount = authenticationImp.findByEmail();
        Optional<Candidate> candidate = candidateRepo.findByAccount_Id(Integer.valueOf(currentAccount.getId()));
        Candidate currentCandidate = candidate.get();
        return currentCandidate;
    }

    public void updateCandidateOrder(CandidateOrder exstingCandidateOrder, String packageName){
        CandidatePackage pkg = packageRepo.findByName(packageName);

        exstingCandidateOrder.setCandidatePackage(pkg);
        exstingCandidateOrder.setAmount(pkg.getPrice());
        exstingCandidateOrder.setStatus(StatusOrder.PAID);
        exstingCandidateOrder.setCreateAt(LocalDate.now());
        exstingCandidateOrder.setActive(true);
        exstingCandidateOrder.setStartDate(LocalDate.now());
        exstingCandidateOrder.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));

        orderRepo.save(exstingCandidateOrder);
    }

    public CandidatePackage getPackageByName(String packageName){
        return packageRepo.findByName(packageName);
    }

    // Check if candidate has an active order
    public boolean hasActivePackage() {
        Candidate currentCandidate = getCurrentCandidate();
        Optional<CandidateOrder> activeOrder = orderRepo.findByCandidate_CandidateIdAndIsActiveTrue(currentCandidate.getCandidateId());
        return activeOrder.isPresent();
    }
}
