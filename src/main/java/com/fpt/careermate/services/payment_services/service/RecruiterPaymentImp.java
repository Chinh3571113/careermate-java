package com.fpt.careermate.services.payment_services.service;

import com.fpt.careermate.common.constant.RecruiterPackageCode;
import com.fpt.careermate.common.constant.StatusPayment;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.common.util.PaymentUtil;
import com.fpt.careermate.config.PaymentConfig;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.order_services.domain.RecruiterInvoice;
import com.fpt.careermate.services.order_services.domain.RecruiterPackage;
import com.fpt.careermate.services.order_services.repository.RecruiterInvoiceRepo;
import com.fpt.careermate.services.order_services.service.RecruiterInvoiceImp;
import com.fpt.careermate.services.payment_services.service.impl.RecruiterPaymentService;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.io.IOException;
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
public class RecruiterPaymentImp implements RecruiterPaymentService {

    PaymentConfig paymentConfig;
    PaymentUtil paymentUtil;
    RecruiterInvoiceRepo recruiterInvoiceRepo;
    RecruiterRepo recruiterRepo;
    AccountRepo accountRepo;
    RecruiterInvoiceImp recruiterInvoiceImp;

    static DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final CoachUtil coachUtil;

    @Override
    public String createPaymentUrl(HttpServletRequest httpServletRequest, String packageName) {
        String email = coachUtil.getCurrentRecruiter().getAccount().getEmail();
        String upperPackageName = packageName.toUpperCase();

        // Kiểm tra nếu là BASIC package thì không được phép thanh toán
        if(upperPackageName.equals(RecruiterPackageCode.BASIC)) throw new AppException(ErrorCode.CAN_NOT_PAY_FOR_BASIC_PACKAGE);

        // Kiểm tra xem recruiter đã có đơn hàng active chưa
        if(recruiterInvoiceImp.hasActivePackage()) throw new AppException(ErrorCode.HAS_ACTIVE_PACKAGE);

        RecruiterPackage recruiterPackage = recruiterInvoiceImp.getPackageByName(upperPackageName);

        HttpServletRequest req = httpServletRequest;
        String bankCode = "NCB";
        String language = "vn";

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long vnpAmount = recruiterPackage.getPrice() * 100L;
        String vnp_TxnRef = paymentUtil.generateTxnRef(8);
        String vnp_IpAddr = paymentUtil.getIpAddress(req);

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnp_Version);
        vnpParams.put("vnp_Command", vnp_Command);
        vnpParams.put("vnp_TmnCode", paymentConfig.vnp_TmnCode.trim());
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");
        if (bankCode != null && !bankCode.isEmpty()) vnpParams.put("vnp_BankCode", bankCode);
        vnpParams.put("vnp_TxnRef", vnp_TxnRef);
        vnpParams.put("vnp_OrderInfo", "CandidateInvoice payment:" + vnp_TxnRef);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", (language == null || language.isEmpty()) ? "vn" : language);
        vnpParams.put("vnp_ReturnUrl", paymentConfig.vnp_RecruiterReturnUrl.trim());
        vnpParams.put("vnp_IpAddr", vnp_IpAddr);
        vnpParams.put("vnp_CreateDate", paymentUtil.nowFormatted());
        vnpParams.put("vnp_ExpireDate", paymentUtil.expireDateFormatted(15));

        vnpParams.put("vnp_OrderInfo", "packageName=" + upperPackageName + "&email=" + email);

        String hashData = paymentUtil.buildHashDataSorted(vnpParams);
        String query = paymentUtil.buildQueryString(vnpParams);

        String secureHash = paymentUtil.hmacSHA512(paymentConfig.secretKey.trim(), hashData);
        query += "&vnp_SecureHash=" + secureHash;

        return paymentConfig.vnp_PayUrl + "?" + query;
    }

    @Override
    @Transactional
    public void paymentReturn(HttpServletRequest request, HttpServletResponse response, Model model) {
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

        // Lấy currentRecruiter
        Optional<Account> exstingAccount = accountRepo.findByEmail(email);
        if(exstingAccount.isEmpty()){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        Optional<Recruiter> exstingRecruiter = recruiterRepo.findByAccount_Id(exstingAccount.get().getId());
        Recruiter recruiter = exstingRecruiter.get();

        // Nếu không tìm thấy recuiter infoce thì là BASIC package
        if(recruiter.getRecruiterInvoice() == null) {
            // Tạo RecruiterInvoice mới
            recruiterInvoiceImp.creatInvoice(packageName, recruiter);
        }
        else {
            // Cập nhật RecruiterInvoice
            // Tìm RecruiterInvoice từ DB
            RecruiterInvoice exstingInvoice = recruiter.getRecruiterInvoice();
            // Cập nhật trạng thái và các thông tin liên quan bằng việc gọi updateRecruiterInvoice method
            recruiterInvoiceImp.updateRecruiterInvoice(exstingInvoice, packageName);
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

        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            log.error("Error redirecting to payment return URL: {}", redirectUrl, e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}
