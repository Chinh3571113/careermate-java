package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.CandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateOrderResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageCandidateOrderResponse;

import java.util.List;

public interface OrderService {
    void cancelOrder(int id);
    PageCandidateOrderResponse getOrderList(int page, int size);
    MyCandidateOrderResponse myCandidatePackage();
}
