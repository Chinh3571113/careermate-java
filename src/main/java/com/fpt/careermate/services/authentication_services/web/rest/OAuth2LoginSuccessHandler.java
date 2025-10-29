package com.fpt.careermate.services.authentication_services.web.rest;

import com.fpt.careermate.common.constant.PredefineRole;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.authentication_services.domain.Role;
import com.fpt.careermate.services.authentication_services.repository.RoleRepo;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AccountRepo accountRepo;
    private final RoleRepo roleRepo;
    private final AuthenticationImp authenticationImp;
    private final RecruiterRepo recruiterRepo;

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // Handle the Google user that Spring Security just authenticated and sync our Account state accordingly.
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        HttpSession session = request.getSession();
        String accountType = (String) session.getAttribute(LoginWithGoogle.ACCOUNT_TYPE_SESSION_KEY);
        boolean isRecruiter = "recruiter".equalsIgnoreCase(accountType);
        session.removeAttribute(LoginWithGoogle.ACCOUNT_TYPE_SESSION_KEY);

        Account account = accountRepo.findByEmail(email).orElse(null);
        if (account == null) {
            account = new Account();
            account.setEmail(email);
            account.setUsername(name);
            account.setPassword("GOOGLE_LOGIN"); // OAuth users do not maintain a local password
        }

        Set<Role> roles = new HashSet<>();
        if (account.getRoles() != null) {
            roles.addAll(account.getRoles());
        }

        if (isRecruiter) {
            // Recruiter intent: attach recruiter role immediately but freeze access until profile + admin approval
            Role recruiterRole = roleRepo.findByName(PredefineRole.RECRUITER_ROLE)
                    .orElseThrow(() -> new RuntimeException("Role RECRUITER not found"));
            roles.add(recruiterRole);
            account.setStatus("PENDING"); // Must await admin approval before login
        } else {
            // Default Google login path becomes a candidate with ACTIVE status if not already set
            Role candidateRole = roleRepo.findByName(PredefineRole.USER_ROLE)
                    .orElseThrow(() -> new RuntimeException("Role USER not found"));
            roles.add(candidateRole);
            if (account.getStatus() == null || account.getStatus().isBlank()) {
                account.setStatus("ACTIVE");
            }
        }

        account.setRoles(roles);
        account = accountRepo.save(account);

        boolean hasRecruiterRole = account.getRoles().stream()
                .anyMatch(role -> PredefineRole.RECRUITER_ROLE.equalsIgnoreCase(role.getName()));
        boolean profileCompleted = hasRecruiterRole &&
                recruiterRepo.findByAccount_Id(account.getId()).isPresent();
        boolean requiresOrgInfo = hasRecruiterRole && !profileCompleted;
        boolean isAccountActive = "ACTIVE".equalsIgnoreCase(account.getStatus());

        String accessToken = null;
        String refreshToken = null;
        if (isAccountActive) {
            accessToken = authenticationImp.generateToken(account, false);
            refreshToken = authenticationImp.generateToken(account, true);
        }

        if (accessToken != null) {
            session.setAttribute("accessToken", accessToken);
            session.setAttribute("refreshToken", refreshToken);
        } else {
            session.removeAttribute("accessToken");
            session.removeAttribute("refreshToken");
        }

        // Store OAuth user data in session for registration completion
        session.setAttribute("oauth_email", email);
        session.setAttribute("oauth_account_id", account.getId());
        session.setAttribute("oauth_timestamp", System.currentTimeMillis());
        session.setAttribute("email", email);
        session.setAttribute("isRecruiter", requiresOrgInfo);
        session.setAttribute("profileCompleted", profileCompleted);

        // Set session to last 30 minutes for OAuth completion flow
        session.setMaxInactiveInterval(1800);

        log.info("OAuth Success - Email: {}, Recruiter: {}, ProfileCompleted: {}, AccountId: {}",
                 email, requiresOrgInfo, profileCompleted, account.getId());

        // Redirect to success endpoint which returns OAuth status as JSON
        // Frontend will call this endpoint to get OAuth result and handle accordingly
        response.sendRedirect("/api/oauth2/google/success");
    }
}
