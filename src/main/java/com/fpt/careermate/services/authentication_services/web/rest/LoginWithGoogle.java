package com.fpt.careermate.services.authentication_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.authentication_services.service.dto.response.GoogleResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name = "Authentication", description = "APIs for Google OAuth2 authentication")
@RequestMapping("/api/oauth2")
@CrossOrigin
@RestController
public class LoginWithGoogle {
    public static final String ACCOUNT_TYPE_SESSION_KEY = "OAUTH_ACCOUNT_TYPE";

    @GetMapping("/google/login")
    public void loginWithGoogle(
            @RequestParam(value = "account_type", required = false) String accountType,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        // Remember the intended account type so the success handler and recruiter completion step can use it
        if ("recruiter".equalsIgnoreCase(accountType)) {
            session.setAttribute(ACCOUNT_TYPE_SESSION_KEY, "recruiter");
        } else {
            session.removeAttribute(ACCOUNT_TYPE_SESSION_KEY);
        }

        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/google/success")
    public ApiResponse<GoogleResponse> googleLoginSuccess(HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        String refreshToken = (String) session.getAttribute("refreshToken");
        String email = (String) session.getAttribute("email");
        Boolean isRecruiter = (Boolean) session.getAttribute("isRecruiter");
        Boolean profileCompleted = (Boolean) session.getAttribute("profileCompleted");

        GoogleResponse tokenResponse = GoogleResponse.builder()
                .email(email)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .recruiter(Boolean.TRUE.equals(isRecruiter))
                .profileCompleted(Boolean.TRUE.equals(profileCompleted))
                .build();

        // Clear temporary tokens from the session; keep email and recruiter flag for registration completion
        session.removeAttribute("accessToken");
        session.removeAttribute("refreshToken");

        int code = (accessToken != null) ? 200 : 202;
        String message = (accessToken != null)
                ? "Login with Google successful"
                : "Recruiter profile required before accessing the system.";

        return ApiResponse.<GoogleResponse>builder()
                .code(code)
                .message(message)
                .result(tokenResponse)
                .build();
    }
}
