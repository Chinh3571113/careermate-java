package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.AuthenticationImp;
import com.fpt.careermate.services.dto.request.AuthenticationRequest;
import com.fpt.careermate.services.dto.request.IntrospectRequest;
import com.fpt.careermate.services.dto.request.LogoutRequest;
import com.fpt.careermate.services.dto.request.RefreshRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.AuthenticationResponse;
import com.fpt.careermate.services.dto.response.IntrospectResponse;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@Tag(name = "Authentication", description = "APIs for authentication and token management")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationImp authenticationServiceImp;

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 days in seconds

    @PostMapping("/token")
    @Operation(summary = "Authenticate User", description = "Authenticate user and generate access and refresh tokens.")
    ApiResponse<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request,
            HttpServletResponse response) {
        var result = authenticationServiceImp.authenticate(request);

        // Set refresh token in HTTP-only cookie
        Cookie refreshTokenCookie = createRefreshTokenCookie(result.getRefreshToken());
        response.addCookie(refreshTokenCookie);

        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @Operation(summary = "Introspect Token", description = "Check the validity of an access token.")
    @PutMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody @Valid IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationServiceImp.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @Operation(summary = "Refresh Token", description = "Refresh access token using refresh token from cookie.")
    @PutMapping("/refresh")
    ApiResponse<AuthenticationResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ParseException, JOSEException {

        // Extract refresh token from cookie
        String refreshToken = extractRefreshTokenFromCookie(request);

        RefreshRequest refreshRequest = RefreshRequest.builder()
                .token(refreshToken)
                .build();

        var result = authenticationServiceImp.refreshToken(refreshRequest);

        // Set new refresh token in cookie
        Cookie refreshTokenCookie = createRefreshTokenCookie(result.getRefreshToken());
        response.addCookie(refreshTokenCookie);

        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @Operation(summary = "Logout", description = "Invalidate the provided access or refresh token.")
    @PostMapping("/logout")
    ApiResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) throws ParseException, JOSEException {

        try {
            // Extract refresh token from cookie
            String refreshToken = extractRefreshTokenFromCookie(request);

            LogoutRequest logoutRequest = LogoutRequest.builder()
                    .token(refreshToken)
                    .build();

            authenticationServiceImp.logout(logoutRequest);
        } catch (Exception e) {
            log.warn("Logout attempted but no valid refresh token found: {}", e.getMessage());
        } finally {
            // Always clear the refresh token cookie
            Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE, null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false); // Set to true in production with HTTPS
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0); // Delete cookie
            response.addCookie(refreshTokenCookie);
        }

        return ApiResponse.<Void>builder().build();
    }

    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true); // Prevent JavaScript access
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_MAX_AGE);
        return cookie;
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
    }
}
