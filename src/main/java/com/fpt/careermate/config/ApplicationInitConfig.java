package com.fpt.careermate.config;

import com.fpt.careermate.common.constant.PredefineRole;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.admin_services.domain.Admin;
import com.fpt.careermate.services.admin_services.repository.AdminRepo;
import com.fpt.careermate.services.authentication_services.domain.Role;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.authentication_services.repository.RoleRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "Le Quang Anh";
    @NonFinal
    static final String ADMIN_USER_EMAIL = "admin@gmail.com";
    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driver-class-name",
            havingValue = "org.postgresql.Driver")
    ApplicationRunner applicationRunner(AccountRepo userRepository, RoleRepo roleRepository, AdminRepo adminRepo) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByEmail(ADMIN_USER_EMAIL).isEmpty()) {
                roleRepository.save(Role.builder()
                        .name(PredefineRole.USER_ROLE)
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(PredefineRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                Account adminAccount = Account.builder()
                        .email(ADMIN_USER_EMAIL)
                        .username(ADMIN_USER_NAME)
                        .status("ACTIVE")
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                adminAccount = userRepository.save(adminAccount);

                // Create Admin profile
                Admin admin = Admin.builder()
                        .name(ADMIN_USER_NAME)
                        .phone("0000000000")
                        .account(adminAccount)
                        .build();

                adminRepo.save(admin);

                log.warn("admin user has been created with default password: admin, please change it");
            }
            log.info("Application initialization completed .....");
        };
    }
}
