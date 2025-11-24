package com.fpt.careermate.services.payment_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.payment_services.service.RecruiterPaymentImp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recruiter - Payment", description = "Manage recruiter pacakge")
@RestController
@RequestMapping("/api/recruiter-payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterPaymentController {

    RecruiterPaymentImp recruiterPaymentImp;

    @Operation(summary = """
            Call GET /order/active API to check if recruiter has an active order.
            If not, create a payment URL for the specified package and return it.
            input: packageName
            output: paymentUrl
            """)
    @PostMapping
    public ApiResponse<String> createPayment(
            @RequestParam String packageName,
            HttpServletRequest httpServletRequest) {
        String paymentUrl = recruiterPaymentImp.createPaymentUrl(httpServletRequest, packageName);

        return ApiResponse.<String>builder()
                .result(paymentUrl)
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Return to backend service to verify payment")
    @GetMapping("/return")
    public void paymentReturn(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Model model){
        recruiterPaymentImp.paymentReturn(httpServletRequest, httpServletResponse, model);
    }
}
