package com.fpt.careermate.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class PaymentUtil {

    public static String hmacSHA512(final String key, final String data) {
        try {
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }

    public String generateTxnRef(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public String nowFormatted() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        return now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public String expireDateFormatted(int minutes) {
        ZonedDateTime t = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusMinutes(minutes);
        return t.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public String buildHashDataSorted(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                if (!first) {
                    hashData.append('&');
                }
                first = false;
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
        }
        return hashData.toString();
    }

    // Build query string (url-encoded)
    public String buildQueryString(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                if (!first) {
                    query.append('&');
                }
                first = false;
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
        }
        return query.toString();
    }

    public String parsePackageNameFromOrderInfo(String orderInfo) {
        if (orderInfo == null) return null;
        String marker = "packageName=";
        int idx = orderInfo.indexOf(marker);
        if (idx >= 0) {
            String sub = orderInfo.substring(idx + marker.length());
            int end = sub.indexOf(' ');
            if (end == -1) end = sub.indexOf('&');
            if (end > 0) sub = sub.substring(0, end);
            return sub.trim();
        }
        return null;
    }

    public String parseEmailFromOrderInfo(String orderInfo) {
        if (orderInfo == null) return null;
        String marker = "email=";
        int idx = orderInfo.indexOf(marker);
        if (idx >= 0) {
            String sub = orderInfo.substring(idx + marker.length());
            int end = sub.indexOf('&');
            if (end > 0) sub = sub.substring(0, end);
            return sub.trim();
        }
        return null;
    }

}
