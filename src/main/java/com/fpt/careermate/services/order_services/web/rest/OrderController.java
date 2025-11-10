package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.services.order_services.service.OrderImp;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.order_services.service.dto.response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "CandidateOrder", description = "Manage candidateOrder")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderImp orderImp;

    @Operation(summary = "Delete candidateOrder by ID")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteOrder(@Positive @PathVariable int id) {
        orderImp.deleteOrder(id);
        return ApiResponse.<String>builder()
                .result("")
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Check candidateOrder status")
    @GetMapping("/status/{id}")
    public ApiResponse<String> checkOrderStatus(@Positive @PathVariable int id) {
        return ApiResponse.<String>builder()
                .result(orderImp.checkOrderStatus(id))
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get candidateOrder list for admin")
    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrderList() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderImp.getOrderList())
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get candidateOrder list for candidate")
    @GetMapping("/my-order")
    public ApiResponse<List<OrderResponse>> myOrderList() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderImp.myOrderList())
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
