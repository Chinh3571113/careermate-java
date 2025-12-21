package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateInvoiceResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyInvoiceListItemResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageMyInvoiceListResponse;

public interface CandidateInvoiceService {
    void cancelMyInvoice();
    MyCandidateInvoiceResponse getMyActiveInvoice();

    PageMyInvoiceListResponse getMyInvoiceHistory(int page, int size);

    MyInvoiceListItemResponse getMyInvoiceById(int invoiceId);
}
