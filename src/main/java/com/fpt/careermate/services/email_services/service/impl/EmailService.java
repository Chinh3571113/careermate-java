package com.fpt.careermate.services.email_services.service.impl;

import com.fpt.careermate.common.util.ChangePassword;
import com.fpt.careermate.common.util.MailBody;

public interface EmailService {
    void sendSimpleEmail(MailBody mailBody);
    void sendSimpleEmailAsync(MailBody mailBody);
    String verifyEmail(String email);
    String verifyOtp(String email, Integer otp);
    String changePassword(ChangePassword password, String email);
}
