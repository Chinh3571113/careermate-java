package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.PageInvoiceListResponse;

public interface AdminInvoiceService {
    PageInvoiceListResponse getAllRecruiterInvoices(
            String status,
            Boolean isActive,
            int page,
            int size
    );

    PageInvoiceListResponse getAllCandidateInvoices(
            String status,
            Boolean isActive,
            int page,
            int size
    );
}
