package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.CandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateOrderResponse;

import java.util.List;

public interface OrderService {
    void cancelOrder(int id);
    List<CandidateOrderResponse> getOrderList();
    MyCandidateOrderResponse myCandidatePackage();
}
