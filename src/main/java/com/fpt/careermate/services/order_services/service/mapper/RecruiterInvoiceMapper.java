package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.RecruiterInvoice;
import com.fpt.careermate.services.order_services.service.dto.response.MyRecruiterInvoiceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecruiterInvoiceMapper {
    @Mapping(source = "recruiterPackage.name", target = "packageName")
    MyRecruiterInvoiceResponse toMyRecruiterInvoiceResponse(RecruiterInvoice recruiterInvoice);
}
