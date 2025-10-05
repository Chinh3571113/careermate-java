package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.GoogleResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Tag(name = "Authentication", description = "APIs for Google OAuth2 authentication")
@RequestMapping("/api/oauth2")
@CrossOrigin
@RestController
public class LoginWithGoogle {
    @GetMapping("/google/login")
    public void  loginWithGoogle(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }


    @GetMapping("/google/success")
    public ApiResponse<GoogleResponse> googleLoginSuccess(HttpSession session) {
        String accessToken = (String) session.getAttribute("accessToken");
        String refreshToken = (String) session.getAttribute("refreshToken");
        String email = (String) session.getAttribute("email");

        if (accessToken == null) {
            return ApiResponse.<GoogleResponse>builder()
                    .code(400)
                    .message("No token found. Please login again.")
                    .build();
        }

        GoogleResponse tokenResponse = GoogleResponse.builder()
                .email(email)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        // Clear session (optional)
        session.removeAttribute("accessToken");
        session.removeAttribute("refreshToken");
        session.removeAttribute("email");

        return ApiResponse.<GoogleResponse>builder()
                .code(200)
                .message("Login with Google successful")
                .result(tokenResponse)
                .build();
    }
}
