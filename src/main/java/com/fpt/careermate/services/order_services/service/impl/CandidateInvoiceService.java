package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateInvoiceResponse;

public interface CandidateInvoiceService {
    void cancelMyInvoice();
    MyCandidateInvoiceResponse getMyActiveInvoice();
}
