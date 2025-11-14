package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.Invoice;
import com.fpt.careermate.services.order_services.service.dto.response.CandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageCandidateOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "candidatePackage.name", target = "packageName")
    MyCandidateOrderResponse toOrderResponse(Invoice invoice);

    @Mapping(source = "candidatePackage.name", target = "packageName")
    @Mapping(source = "candidate.fullName", target = "candidateName")
    CandidateOrderResponse toCandidateOrderResponse(Invoice invoice);

    // Chuyển page sang PageCandidateOrderResponse
    PageCandidateOrderResponse toPageCandidateOrderResponse(Page<Invoice> candidateOrderPage);
}
