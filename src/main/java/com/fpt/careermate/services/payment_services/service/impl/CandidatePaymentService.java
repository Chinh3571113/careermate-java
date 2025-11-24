package com.fpt.careermate.services.payment_services.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;

public interface CandidatePaymentService {
    String createPaymentUrl(HttpServletRequest httpServletRequest, String orderCode);
    void paymentReturn(HttpServletRequest request, HttpServletResponse response, Model model);
}
