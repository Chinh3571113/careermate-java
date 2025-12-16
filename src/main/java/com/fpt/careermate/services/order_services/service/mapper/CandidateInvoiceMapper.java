package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.CandidateInvoice;
import com.fpt.careermate.services.order_services.service.dto.response.CandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateInvoiceResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyInvoiceListItemResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageCandidateOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface CandidateInvoiceMapper {
    @Mapping(source = "candidatePackage.name", target = "packageName")
    MyCandidateInvoiceResponse toMyCandidateInvoiceResponse(CandidateInvoice candidateInvoice);

    @Mapping(source = "candidatePackage.name", target = "packageName")
    MyInvoiceListItemResponse toMyInvoiceListItemResponse(CandidateInvoice candidateInvoice);
}
