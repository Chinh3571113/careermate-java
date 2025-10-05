package com.fpt.careermate.services;

import com.fpt.careermate.domain.Order;
import com.fpt.careermate.domain.Package;
import com.fpt.careermate.domain.TestCandidate;
import com.fpt.careermate.domain.enums.OrderStatus;
import com.fpt.careermate.repository.OrderRepo;
import com.fpt.careermate.repository.PackageRepo;
import com.fpt.careermate.services.dto.request.OrderCreationRequest;
import com.fpt.careermate.services.dto.response.OrderResponse;
import com.fpt.careermate.services.impl.OrderService;
import com.fpt.careermate.services.mapper.OrderMapper;
import com.fpt.careermate.util.PaymentUtil;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderImp implements OrderService {

    OrderRepo orderRepo;
    PackageRepo packageRepo;
    OrderMapper orderMapper;
    PaymentUtil paymentUtil;

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    @Transactional
    public String createOrder(OrderCreationRequest request) {
        //TODO: check if candidate exists
        TestCandidate candidate = new TestCandidate(1, null, null);

        Package pkg = packageRepo.findById(request.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        Order order = new Order();
        // TODO: replace candidate with actual candidate
        order.setOrderCode(paymentUtil.generateOrderCodeUuid());
        order.setCandidate(candidate);
        order.setCandidatePackage(pkg);
        order.setAmount(pkg.getPrice());
        order.setPackageNameSnapshot(pkg.getName());
        order.setPackagePriceSnapshot(pkg.getPrice());
        order.setStatus(OrderStatus.PENDING.toString());
        order.setCreateAt(LocalDate.now());
        Order savedOrder = orderRepo.save(order);

        return savedOrder.getOrderCode();
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public void deleteOrder(int id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getStatus().equals(OrderStatus.PENDING.toString())) {
            throw new AppException(ErrorCode.CANNOT_DELETE_ORDER);
        }

        order.setStatus(OrderStatus.CANCELLED.toString());
        order.setCancelledAt(LocalDate.now());
        orderRepo.save(order);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public String checkOrderStatus(int id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        return order.getStatus();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<OrderResponse> getOrderList() {
        return orderMapper.toOrderResponseList(orderRepo.findAll());
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public List<OrderResponse> myOrderList() {
        int candidateId = 1; // TODO: replace with actual candidate id
        List<Order> orders = orderRepo.findByCandidate_Id(candidateId);
        return orderMapper.toOrderResponseList(orders);
    }
}
