package com.fpt.careermate.services.authentication_services.service;

import com.fpt.careermate.common.constant.StatusAccount;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.domain.InvalidToken;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.authentication_services.repository.InvalidDateTokenRepo;
import com.fpt.careermate.services.authentication_services.service.dto.request.AuthenticationRequest;
import com.fpt.careermate.services.authentication_services.service.dto.request.IntrospectRequest;
import com.fpt.careermate.services.authentication_services.service.dto.request.LogoutRequest;
import com.fpt.careermate.services.authentication_services.service.dto.request.RefreshRequest;
import com.fpt.careermate.services.authentication_services.service.dto.response.AuthenticationResponse;
import com.fpt.careermate.services.authentication_services.service.dto.response.IntrospectResponse;
import com.fpt.careermate.services.authentication_services.service.impl.AuthenticationService;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationImp implements AuthenticationService {
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    protected final InvalidDateTokenRepo invalidatedTokenRepository;

    private final AccountRepo accountRepo;

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    @Override
    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = accountRepo
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        String status = user.getStatus();
        if (!StatusAccount.ACTIVE.equalsIgnoreCase(status)) {
            throw new AppException(ErrorCode.USER_INACTIVE);
        }

        String accessToken = generateToken(user, false);
        String refreshToken = generateToken(user, true);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .expiresIn(VALID_DURATION)
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        int invalidatedCount = 0;

        // 🧩 1. Invalidate refresh token (if exists)
        if (request.getToken() != null && !request.getToken().trim().isEmpty()) {
            try {
                var signToken = verifyToken(request.getToken());
                invalidateToken(signToken);
                invalidatedCount++;
                log.info("Refresh token invalidated successfully");
            } catch (AppException e) {
                log.info("Refresh token already expired or invalid");
            }
        }

        // 🧩 2. (Optional) Invalidate access token from SecurityContext
        try {
            var context = SecurityContextHolder.getContext();
            var auth = context.getAuthentication();
            if (auth != null && auth.getCredentials() != null) {
                String accessToken = auth.getCredentials().toString();
                try {
                    var signToken = verifyToken(accessToken);
                    invalidateToken(signToken);
                    invalidatedCount++;
                    log.info("Access token invalidated successfully");
                } catch (AppException e) {
                    log.info("Access token already expired or invalid");
                }
            }
        } catch (Exception e) {
            log.debug("No active access token in context");
        }

        log.info("Logout completed. {} token(s) invalidated", invalidatedCount);
    }

    private void invalidateToken(SignedJWT signToken) throws ParseException {
        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidatedToken = InvalidToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken());
        var jti = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Check if RT already used (token reuse detection)
        if (invalidatedTokenRepository.existsById(jti)) {
            throw new AppException(ErrorCode.TOKEN_REUSE_DETECTED);
        }

        // Invalidate old RT
        InvalidToken invalidatedToken = InvalidToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);

        // Generate new tokens
        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = accountRepo.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        String newAccessToken = generateToken(user, false);
        String newRefreshToken = generateToken(user, true);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .expiresIn(VALID_DURATION)
                .tokenType("Bearer")
                .build();
    }


    @Override
    public String generateToken(Account account, boolean isRefresh) {
        long validDuration = (isRefresh) ? REFRESHABLE_DURATION : VALID_DURATION;
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getEmail())
                .issuer("careermate.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("fullname", account.getUsername())
                .claim("scope", buildScope(account))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public String buildScope(Account account) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(account.getRoles()))
            account.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }

    @Override
    public Account findByEmail() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        return accountRepo.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

}
