package com.fpt.careermate.services.payment_services.web.rest;

import com.fpt.careermate.services.payment_services.service.PaymentImp;
import com.fpt.careermate.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment", description = "Manage payment")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentController {

    private final PaymentImp paymentImp;

    @Operation(summary = """
            Call GET /order/active API to check if candidate has an active order.
            If not, create a payment URL for the specified package and return it.
            input: packageName
            output: paymentUrl
            """)
    @PostMapping
    public ApiResponse<String> createPayment(
            @RequestParam String packageName,
            HttpServletRequest httpServletRequest) {
        String paymentUrl = paymentImp.createPaymentUrl(httpServletRequest, packageName);

        return ApiResponse.<String>builder()
                .result(paymentUrl)
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Return to backend service to verify payment")
    @GetMapping("/return")
    public ApiResponse<String> paymentReturn(HttpServletRequest httpServletRequest, Model model){
        return ApiResponse.<String>builder()
                .result(paymentImp.paymentReturn(httpServletRequest, model))
                .code(200)
                .build();
    }
}
