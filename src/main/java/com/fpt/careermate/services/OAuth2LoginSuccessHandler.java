package com.fpt.careermate.services;

import com.fpt.careermate.constant.PredefineRole;
import com.fpt.careermate.domain.Account;
import com.fpt.careermate.domain.Role;
import com.fpt.careermate.repository.AccountRepo;
import com.fpt.careermate.repository.RoleRepo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AccountRepo accountRepo;
    private final RoleRepo roleRepo;
    private final AuthenticationImp authenticationImp;

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Tạo hoặc lấy Account từ DB
        Account account = accountRepo.findByEmail(email).orElseGet(() -> {
            Account acc = new Account();
            acc.setEmail(email);
            acc.setUsername(name);
            acc.setPassword("GOOGLE_LOGIN"); // hoặc random password, vì Google login không có password
            acc.setStatus("ACTIVE");

            // Gán role mặc định
            Role candidateRole = roleRepo.findByName(PredefineRole.USER_ROLE)
                    .orElseThrow(() -> new RuntimeException("Role USER not found"));
            acc.setRoles(new HashSet<>(Set.of(candidateRole)));

            return accountRepo.save(acc);
        });



        // Tạo token dựa trên thông tin account
        String accessToken = authenticationImp.generateToken(account, false);
        String refreshToken = authenticationImp.generateToken(account, true);

        // Lưu token vào session tạm để redirect
        HttpSession session = request.getSession();
        session.setAttribute("accessToken", accessToken);
        session.setAttribute("refreshToken", refreshToken);
        session.setAttribute("email", email);

        // Redirect về controller để trả token JSON
        response.sendRedirect("/api/oauth2/google/success");
    }
}
