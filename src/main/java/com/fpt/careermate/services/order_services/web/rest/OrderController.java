package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.services.order_services.service.OrderImp;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.order_services.service.dto.response.CandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageCandidateOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "Invoice", description = "Manage invoice")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderImp orderImp;

    @Operation(summary = """
            Cancel candidate package by id
            input: invoice id
            output: success message
            """)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelOrder(@Positive @PathVariable int id) {
        orderImp.cancelOrder(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get invoice list for admin")
    @GetMapping
    public ApiResponse<PageCandidateOrderResponse> getOrderList(
            @RequestParam(name = "page", defaultValue = "1") @Positive int page,
            @RequestParam(name = "size", defaultValue = "10") @Positive int size
    ) {
        return ApiResponse.<PageCandidateOrderResponse>builder()
                .result(orderImp.getOrderList(page, size))
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get invoice list for candidate")
    @GetMapping("/my-pacakge")
    public ApiResponse<MyCandidateOrderResponse> myOrderList() {
        return ApiResponse.<MyCandidateOrderResponse>builder()
                .result(orderImp.myCandidatePackage())
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = """
            Call this API before call POST /api/payment
            to check if candidate has an active package.
            input: none
            output: true if candidate has active package, false if not
            """)
    @GetMapping("/active-package")
    public ApiResponse<Boolean> hasActivePackage() {
        return ApiResponse.<Boolean>builder()
                .result(orderImp.hasActivePackage())
                .code(200)
                .message("success")
                .build();
    }
}
