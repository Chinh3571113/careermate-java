package com.fpt.careermate.services.payment_services.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.config.PaymentConfig;
import com.fpt.careermate.common.constant.StatusPayment;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.order_services.domain.Invoice;
import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.order_services.service.OrderImp;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.order_services.repository.CandidateOrderRepo;
import com.fpt.careermate.services.payment_services.service.impl.PaymentService;
import com.fpt.careermate.common.util.PaymentUtil;
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
    CandidateOrderRepo candidateOrderRepo;
    CandidateRepo candidateRepo;
    AccountRepo accountRepo;
    OrderImp orderImp;

    static DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final CoachUtil coachUtil;

    @Override
    public String createPaymentUrl(HttpServletRequest httpServletRequest, String packageName) {
        String email = coachUtil.getCurrentCandidate().getAccount().getEmail();
        String upperPackageName = packageName.toUpperCase();

        // Kiểm tra nếu là FREE package thì không được phép thanh toán
        if(upperPackageName.equals("FREE")) throw new AppException(ErrorCode.CAN_NOT_PAY_FOR_FREE_PACKAGE);

        // Kiểm tra xem candidate đã có đơn hàng active chưa
        if(orderImp.hasActivePackage()) throw new AppException(ErrorCode.HAS_ACTIVE_PACKAGE);

        CandidatePackage candidatePackage = orderImp.getPackageByName(upperPackageName);

        HttpServletRequest req = httpServletRequest;
        String bankCode = "NCB";
        String language = "vn";

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long vnpAmount = candidatePackage.getPrice() * 100L;
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
        vnpParams.put("vnp_OrderInfo", "Invoice payment:" + vnp_TxnRef);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", (language == null || language.isEmpty()) ? "vn" : language);
        vnpParams.put("vnp_ReturnUrl", paymentConfig.vnp_ReturnUrl);
        vnpParams.put("vnp_IpAddr", vnp_IpAddr);
        vnpParams.put("vnp_CreateDate", paymentUtil.nowFormatted());
        vnpParams.put("vnp_ExpireDate", paymentUtil.expireDateFormatted(15));

        vnpParams.put("vnp_OrderInfo", "packageName=" + upperPackageName + "&email=" + email);

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

        // Payment success or failed
        if (!valid) {
            serverStatus = StatusPayment.INVALID_HASH;
        } else {
            if ("00".equals(vnpResponse)) {
                serverStatus = StatusPayment.SUCCESS;
            } else {
                serverStatus = "failed_" + (vnpResponse == null ? "unknown" : vnpResponse);
            }
        }

        if(!serverStatus.equalsIgnoreCase(StatusPayment.SUCCESS)) throw new AppException(ErrorCode.PAYMENT_FAILED);

        // Lấy email và packageName
        String email = null;
        if (!vnpOrderInfo.isEmpty()) {
            email = paymentUtil.parseEmailFromOrderInfo(vnpOrderInfo);
        }
        String packageName = paymentUtil.parsePackageNameFromOrderInfo(vnpOrderInfo);

        // Lấy currentCandidate
        Optional<Account> exstingAccount = accountRepo.findByEmail(email);
        if(exstingAccount.isEmpty()){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        Optional<Candidate> exstingCandidate = candidateRepo.findByAccount_Id(exstingAccount.get().getId());
        Candidate candidate = exstingCandidate.get();

        // Nếu không tìm thấy invoice thì là Free package
        if(candidate.getInvoice() == null) {
            // Tạo Invoice mới
            orderImp.createOrder(packageName, candidate);
        }
        else {
            // Cập nhật Invoice
            // Tìm invoice từ DB
            Invoice exstingInvoice = candidate.getInvoice();
            // Cập nhật trạng thái và các thông tin liên quan bằng việc gọi updateCandidateOrder method
            orderImp.updateCandidateOrder(exstingInvoice, packageName);
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
