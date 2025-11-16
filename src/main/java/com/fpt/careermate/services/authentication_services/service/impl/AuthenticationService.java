package com.fpt.careermate.services.authentication_services.service.impl;

import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.dto.request.AuthenticationRequest;
import com.fpt.careermate.services.authentication_services.service.dto.request.IntrospectRequest;
import com.fpt.careermate.services.authentication_services.service.dto.request.LogoutRequest;
import com.fpt.careermate.services.authentication_services.service.dto.request.RefreshRequest;
import com.fpt.careermate.services.authentication_services.service.dto.response.AuthenticationResponse;
import com.fpt.careermate.services.authentication_services.service.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;

public interface AuthenticationService {
    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
    SignedJWT verifyToken(String token) throws JOSEException, ParseException;
    AuthenticationResponse authenticate(AuthenticationRequest request);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
    String generateToken(Account account,boolean isRefresh) throws JOSEException;
    String buildScope(Account account);
    Account findByEmail();
    AuthenticationResponse authenticateCandidate(AuthenticationRequest request);



}
