package com.fpt.careermate.services.email_services.service;

import com.fpt.careermate.common.util.MailBody;
import com.fpt.careermate.services.email_services.service.impl.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AsyncEmailService {

    EmailService emailService;

    /**
     * Send email asynchronously to avoid blocking the main thread
     * This improves API response time by 2-5 seconds
     * Uses dedicated emailTaskExecutor thread pool
     */
    @Async("emailTaskExecutor")
    public void sendEmailAsync(MailBody mailBody) {
        try {
            emailService.sendSimpleEmail(mailBody);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", mailBody.to(), e);
        }
    }
}

