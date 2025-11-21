package com.fpt.careermate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Value("${vnpay.pay-url}")
    public String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    public String vnp_ReturnUrl;

    @Value("${vnpay.tmn-code}")
    public String vnp_TmnCode;

    @Value("${vnpay.secret-key}")
    public String secretKey;

    @Value("${vnpay.recruiter-return-url}")
    public String vnp_RecruiterReturnUrl;
}

