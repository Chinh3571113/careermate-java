package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.MyRecruiterInvoiceResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyInvoiceListItemResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageMyInvoiceListResponse;

public interface RecruiterInvoiceService {
    void cancelMyInvoice();
    MyRecruiterInvoiceResponse getMyActiveInvoice();

    PageMyInvoiceListResponse getMyInvoiceHistory(int page, int size);

    MyInvoiceListItemResponse getMyInvoiceById(int invoiceId);
}
