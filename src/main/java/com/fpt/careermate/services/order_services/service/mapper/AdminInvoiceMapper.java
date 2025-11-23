package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.CandidateInvoice;
import com.fpt.careermate.services.order_services.domain.RecruiterInvoice;
import com.fpt.careermate.services.order_services.service.dto.response.InvoiceListResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageInvoiceListResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminInvoiceMapper {
    // Recruiter Invoice mapping
    @Mapping(source = "recruiter.account.username", target = "fullname")
    @Mapping(source = "recruiterPackage.name", target = "packageName")
    InvoiceListResponse toAdminRecruiterInvoiceResponse(RecruiterInvoice recruiterInvoice);
    List<InvoiceListResponse> toAdminRecruiterInvoiceResponse(List<RecruiterInvoice> recruiterInvoices);

    PageInvoiceListResponse toAdminRecruiterInvoiceResponsePage(Page<RecruiterInvoice> recruiterInvoices);

    // Candidate Invoice mapping
    @Mapping(source = "candidate.fullName", target = "fullname")
    @Mapping(source = "candidatePackage.name", target = "packageName")
    InvoiceListResponse toAdminCandidateInvoiceResponse(CandidateInvoice candidateInvoice);
    List<InvoiceListResponse> toAdminCandidateInvoiceResponse(List<CandidateInvoice> candidateInvoices);

    PageInvoiceListResponse toAdminCandidateInvoiceResponsePage(Page<CandidateInvoice> candidateInvoices);
}
