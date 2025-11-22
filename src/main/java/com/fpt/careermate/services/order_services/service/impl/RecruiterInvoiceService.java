package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.MyRecruiterInvoiceResponse;

public interface RecruiterInvoiceService {
    void cancelMyInvoice();
    MyRecruiterInvoiceResponse getMyActiveInvoice();
}
