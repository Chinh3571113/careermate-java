package com.fpt.careermate.services.payment_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.payment_services.service.RecruiterPaymentImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecruiterPaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RecruiterPaymentController Tests")
class RecruiterPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecruiterPaymentImp recruiterPaymentImp;

    @BeforeEach
    void setUp() {
    }

    @Nested
    @DisplayName("POST /api/recruiter-payment - Create Payment")
    class CreatePaymentTests {
        @Test
        @DisplayName("Should create payment URL successfully")
        void createPayment_ReturnsSuccess() throws Exception {
            when(recruiterPaymentImp.createPaymentUrl(any(HttpServletRequest.class), anyString()))
                    .thenReturn("https://payment.example.com/pay/123");

            mockMvc.perform(post("/api/recruiter-payment")
                            .param("packageName", "Premium"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result").value("https://payment.example.com/pay/123"));
        }
    }
}
