package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.RecruiterInvoice;
import com.fpt.careermate.services.order_services.service.dto.response.AdminRecruiterInvoiceResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyRecruiterInvoiceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecruiterInvoiceMapper {
    @Mapping(source = "recruiterPackage.name", target = "packageName")
    MyRecruiterInvoiceResponse toMyRecruiterInvoiceResponse(RecruiterInvoice recruiterInvoice);

    @Mapping(source = "recruiter.id", target = "recruiterId")
    @Mapping(source = "recruiter.companyName", target = "recruiterCompanyName")
    @Mapping(source = "recruiter.account.email", target = "recruiterEmail")
    @Mapping(source = "recruiterPackage.name", target = "packageName")
    AdminRecruiterInvoiceResponse toAdminRecruiterInvoiceResponse(RecruiterInvoice recruiterInvoice);
}
