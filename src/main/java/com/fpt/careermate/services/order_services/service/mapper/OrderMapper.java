package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.CandidateOrder;
import com.fpt.careermate.services.order_services.service.dto.response.CandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "candidatePackage.name", target = "packageName")
    MyCandidateOrderResponse toOrderResponse(CandidateOrder candidateOrder);

    List<CandidateOrderResponse> toCandidateOrderResponseResponseList (List<CandidateOrder> candidateOrders);
}
