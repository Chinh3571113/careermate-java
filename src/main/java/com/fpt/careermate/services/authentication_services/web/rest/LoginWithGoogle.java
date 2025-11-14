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
            @RequestParam(value = "redirect_url", required = false) String redirectUrl,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        // Remember the intended account type so the success handler and recruiter completion step can use it
        if ("recruiter".equalsIgnoreCase(accountType)) {
            session.setAttribute(ACCOUNT_TYPE_SESSION_KEY, "recruiter");
        } else {
            session.removeAttribute(ACCOUNT_TYPE_SESSION_KEY);
        }

        // Store frontend redirect URL for after OAuth completes
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            session.setAttribute("oauth_redirect_url", redirectUrl);
        } else {
            // Default frontend URL
            session.setAttribute("oauth_redirect_url", "http://localhost:3000");
        }

        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/google/success")
    public void googleLoginSuccess(HttpSession session, HttpServletResponse response) throws IOException {
        String accessToken = (String) session.getAttribute("accessToken");
        String refreshToken = (String) session.getAttribute("refreshToken");
        String email = (String) session.getAttribute("email");
        Boolean isRecruiter = (Boolean) session.getAttribute("isRecruiter");
        Boolean profileCompleted = (Boolean) session.getAttribute("profileCompleted");
        String accountStatus = (String) session.getAttribute("accountStatus"); // Get account status
        String redirectUrl = (String) session.getAttribute("oauth_redirect_url");

        // Default to localhost if no redirect URL was stored
        if (redirectUrl == null || redirectUrl.isBlank()) {
            redirectUrl = "http://localhost:3000";
        }

        // Build redirect URL with OAuth result as query parameters
        StringBuilder finalUrl = new StringBuilder(redirectUrl);

        // Add /oauth-callback path if not already present
        if (!redirectUrl.contains("/oauth-callback") && !redirectUrl.endsWith("/")) {
            finalUrl.append("/oauth-callback");
        } else if (redirectUrl.endsWith("/")) {
            finalUrl.append("oauth-callback");
        }

        finalUrl.append("?success=true");

        if (email != null) {
            finalUrl.append("&email=").append(java.net.URLEncoder.encode(email, "UTF-8"));
        }

        // Determine account type first - this is critical for proper redirect handling
        boolean isRecruiterAccount = Boolean.TRUE.equals(isRecruiter);
        boolean hasCompletedProfile = Boolean.TRUE.equals(profileCompleted);

        // Set account_type parameter
        if (isRecruiterAccount) {
            finalUrl.append("&account_type=recruiter");
            finalUrl.append("&profile_completed=").append(hasCompletedProfile);
        } else {
            finalUrl.append("&account_type=candidate");
        }

        // Set status and tokens based on account state
        // Include account status (ACTIVE, PENDING, REJECTED, BANNED) in the redirect
        if (accountStatus != null) {
            finalUrl.append("&account_status=").append(accountStatus.toLowerCase());
        }

        if (accessToken != null) {
            // Tokens were generated (account is not BANNED)
            finalUrl.append("&access_token=").append(java.net.URLEncoder.encode(accessToken, "UTF-8"));
            finalUrl.append("&refresh_token=").append(java.net.URLEncoder.encode(refreshToken, "UTF-8"));
        } else if (isRecruiterAccount && !hasCompletedProfile) {
            // Recruiter needs to complete organization info (should have accountStatus=PENDING)
            finalUrl.append("&status=registration_required");
        }


        // Clear temporary tokens from the session; keep email and recruiter flag for registration completion
        session.removeAttribute("accessToken");
        session.removeAttribute("refreshToken");
        session.removeAttribute("oauth_redirect_url");

        response.sendRedirect(finalUrl.toString());
    }

    @GetMapping("/google/status")
    public ApiResponse<GoogleResponse> getGoogleLoginStatus(HttpSession session) {
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

    @GetMapping("/google/error")
    public void googleLoginError(
            @RequestParam(value = "reason", required = false) String reason,
            @RequestParam(value = "existing_role", required = false) String existingRole,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        String errorMessage = (String) session.getAttribute("oauth_error");
        String redirectUrl = (String) session.getAttribute("oauth_redirect_url");

        if (redirectUrl == null || redirectUrl.isBlank()) {
            redirectUrl = "http://localhost:3000";
        }

        // Build error redirect URL
        StringBuilder finalUrl = new StringBuilder(redirectUrl);
        if (!redirectUrl.contains("/oauth-callback") && !redirectUrl.endsWith("/")) {
            finalUrl.append("/oauth-callback");
        } else if (redirectUrl.endsWith("/")) {
            finalUrl.append("oauth-callback");
        }

        finalUrl.append("?success=false");
        finalUrl.append("&error=").append(reason != null ? reason : "unknown");

        if (existingRole != null) {
            finalUrl.append("&existing_role=").append(existingRole);
        }

        if (errorMessage != null) {
            finalUrl.append("&message=").append(java.net.URLEncoder.encode(errorMessage, "UTF-8"));
        }

        // Clear error from session
        session.removeAttribute("oauth_error");
        session.removeAttribute("oauth_redirect_url");

        response.sendRedirect(finalUrl.toString());
    }
}
