package com.fpt.careermate.services;

import com.fpt.careermate.config.PaymentConfig;
import com.fpt.careermate.constant.StatusOrder;
import com.fpt.careermate.constant.StatusPayment;
import com.fpt.careermate.domain.Candidate;
import com.fpt.careermate.domain.Order;
import com.fpt.careermate.domain.Package;
import com.fpt.careermate.domain.Payment;
import com.fpt.careermate.repository.CandidateRepo;
import com.fpt.careermate.repository.OrderRepo;
import com.fpt.careermate.repository.PaymentRepo;
import com.fpt.careermate.services.impl.PaymentService;
import com.fpt.careermate.util.PaymentUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentImp implements PaymentService {

    PaymentConfig paymentConfig;
    PaymentUtil paymentUtil;
    PaymentRepo paymentRepo;
    OrderRepo orderRepo;
    CandidateRepo candidateRepo;

    static DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String createPaymentUrl(HttpServletRequest httpServletRequest, long amount, String orderCode) {
        HttpServletRequest req = httpServletRequest;
        String bankCode = "NCB";
        String language = "vn";

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long vnpAmount = amount * 100;
        String vnp_TxnRef = paymentUtil.generateTxnRef(8);
        String vnp_IpAddr = paymentUtil.getIpAddress(req);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnp_Version);
        vnpParams.put("vnp_Command", vnp_Command);
        vnpParams.put("vnp_TmnCode", paymentConfig.vnp_TmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");
        if (bankCode != null && !bankCode.isEmpty()) vnpParams.put("vnp_BankCode", bankCode);
        vnpParams.put("vnp_TxnRef", vnp_TxnRef);
        vnpParams.put("vnp_OrderInfo", "Order payment:" + vnp_TxnRef);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", (language == null || language.isEmpty()) ? "vn" : language);
        vnpParams.put("vnp_ReturnUrl", paymentConfig.vnp_ReturnUrl);
        vnpParams.put("vnp_IpAddr", vnp_IpAddr);
        vnpParams.put("vnp_CreateDate", paymentUtil.nowFormatted());
        vnpParams.put("vnp_ExpireDate", paymentUtil.expireDateFormatted(15));

        vnpParams.put("vnp_OrderInfo", "orderCode=" + orderCode);

        String hashData = paymentUtil.buildHashDataSorted(vnpParams);
        String query = paymentUtil.buildQueryString(vnpParams);

        String secureHash = paymentUtil.hmacSHA512(paymentConfig.secretKey, hashData);
        query += "&vnp_SecureHash=" + secureHash;

        return paymentConfig.vnp_PayUrl + "?" + query;
    }

    @Override
    @Transactional
    public String paymentReturn(HttpServletRequest request, Model model) {
        Map<String, String> fields = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        String vnp_SecureHash = null;

        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            if (values.length > 0) {
                String value = values[0];
                if ("vnp_SecureHash".equalsIgnoreCase(key)) {
                    vnp_SecureHash = value;
                } else {
                    fields.put(key, value);
                }
            }
        }

        // Build hash data and compute checksum
        String hashData = paymentUtil.buildHashDataSorted(fields);
        String checkSum = paymentUtil.hmacSHA512(paymentConfig.secretKey, hashData);

        boolean valid = checkSum.equalsIgnoreCase(vnp_SecureHash);
        String serverStatus;
        String vnpResponse = fields.get("vnp_ResponseCode");
        String vnpTxnRef = fields.get("vnp_TxnRef");
        String vnpOrderInfo = fields.get("vnp_OrderInfo");
        String vnpAmountStr = fields.get("vnp_Amount");
        long amount = 0L;
        if (vnpAmountStr != null) {
            try {
                amount = Long.parseLong(vnpAmountStr) / 100L;
            } catch (NumberFormatException ex) {
                log.warn("Cannot parse vnp_Amount: {}", vnpAmountStr);
            }
        }
        String vnpTransactionNo = fields.get("vnp_TransactionNo");
        String vnpPayDateStr = fields.get("vnp_PayDate");
        LocalDate vnpPayDate = null;
        try {
            if (vnpPayDateStr != null && !vnpPayDateStr.isEmpty()) {
                vnpPayDate = LocalDate.parse(vnpPayDateStr, VNP_DATE_FORMAT);
            }
        } catch (Exception ex) {
            log.warn("Cannot parse vnp_PayDate: {}", vnpPayDateStr, ex);
        }

        Order order = null;
        if (!vnpOrderInfo.isEmpty()) {
            String orderCode = paymentUtil.parseOrderCodeFromOrderInfo(vnpOrderInfo);
            if (orderCode != null) {
                Optional<Order> maybe = orderRepo.findByOrderCode(orderCode);
                if (maybe.isPresent()) order = maybe.get();
            }
        }

        if(order.getStatus().equals(StatusOrder.CANCELLED)){
            return "cancelled";
        }

        if (!valid) {
            serverStatus = StatusPayment.INVALID_HASH;
        } else {
            if ("00".equals(vnpResponse)) {
                serverStatus = StatusPayment.SUCCESS;
            } else {
                serverStatus = "failed_" + (vnpResponse == null ? "unknown" : vnpResponse);
            }
        }

        try {
            Optional<Payment> maybePayment = Optional.empty();
            if (order != null) {
                maybePayment = paymentRepo.findByOrder(order);
            }

            Payment payment = maybePayment.orElseGet(Payment::new);
            payment.setTxnRef(vnpTxnRef);
            payment.setTransactionNo(vnpTransactionNo);
            payment.setAmount(amount);
            payment.setResponseCode(vnpResponse);
            payment.setBankCode(fields.get("vnp_BankCode"));
            payment.setPayDate(vnpPayDate);
            payment.setRawResponse(fields.toString());

            // validate payment status
            if (!valid) {
                payment.setStatus(StatusPayment.INVALID_HASH);
            } else if ("00".equals(vnpResponse)) {
                payment.setStatus(StatusPayment.SUCCESS);
            } else {
                payment.setStatus(StatusPayment.FAILED);
            }

            // 3) assign relation payment -> found order
            if (order != null) {
                payment.setOrder(order);
            }

            // 4) Save payment (idempotency: if success, not duplicate)
            // old payment is success -> skip updating order
            if (maybePayment.isPresent()) {
                Payment existing = maybePayment.get();

                if (!existing.getStatus().equals(StatusPayment.SUCCESS)) {
                    existing.setTransactionNo(payment.getTransactionNo());
                    existing.setAmount(payment.getAmount());
                    existing.setResponseCode(payment.getResponseCode());
                    existing.setBankCode(payment.getBankCode());
                    existing.setPayDate(payment.getPayDate());
                    existing.setRawResponse(payment.getRawResponse());
                    existing.setStatus(payment.getStatus());
                    if (order != null) existing.setOrder(order);
                    payment = paymentRepo.save(existing);
                }
            } else {
                payment = paymentRepo.save(payment);
            }

            // 5) if payment success -> update order & candidate
            if (payment.getStatus().equals(StatusPayment.SUCCESS) && order != null) {
                if (!order.getStatus().equals(StatusOrder.PAID)) {
                    LocalDate now = LocalDate.now();

                    order.setStatus(StatusOrder.PAID);
                    order.setStartDate(now);
                    Package pkg = order.getCandidatePackage();
                    order.setEndDate(now.plusDays(pkg.getDurationDays()));
                    orderRepo.save(order);

                    Candidate candidate = order.getCandidate();
                    if (candidate != null && pkg != null) {
                        candidate.setCurrentPackage(pkg);
                        candidateRepo.save(candidate);
                    }
                } else {
                    log.info("Order {} already PAID, skipping applying package", order.getOrderCode());
                }
            }
        } catch (Exception ex) {
            log.error("Error saving payment/update order: {}", ex.getMessage(), ex);
        }

        // --- Build redirect query (forward original params except vnp_SecureHash) + serverVerified info ---
        StringBuilder qs = new StringBuilder();
        try {
            boolean first = true;
            for (Map.Entry<String, String[]> e : requestParams.entrySet()) {
                String key = e.getKey();
                if ("vnp_SecureHash".equalsIgnoreCase(key)) continue;
                String[] vals = e.getValue();
                if (vals == null || vals.length == 0) continue;
                String val = vals[0];
                if (!first) qs.append('&');
                first = false;
                qs.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()));
                qs.append('=');
                qs.append(URLEncoder.encode(val, StandardCharsets.UTF_8.toString()));
            }

            if (qs.length() > 0) qs.append('&');
            qs.append("serverVerified=").append(URLEncoder.encode(String.valueOf(valid), StandardCharsets.UTF_8.toString()));
            qs.append('&');
            qs.append("serverStatus=").append(URLEncoder.encode(serverStatus, StandardCharsets.UTF_8.toString()));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        String redirectUrl = "http://localhost:3000/payment/return?" + qs.toString();
        return "redirect:" + redirectUrl;
    }
}
