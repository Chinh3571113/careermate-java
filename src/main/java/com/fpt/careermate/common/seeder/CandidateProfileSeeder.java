package com.fpt.careermate.common.seeder;

import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.authentication_services.domain.Role;
import com.fpt.careermate.services.authentication_services.repository.RoleRepo;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * 🌱 CandidateProfileSeeder
 *
 * Seed dữ liệu cho candidate profiles
 * Kiểm tra nếu có 1 profile thì tạo thêm 3 profiles
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidateProfileSeeder implements CommandLineRunner {

    CandidateRepo candidateRepo;
    AccountRepo accountRepo;
    RoleRepo roleRepo;
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedCandidateProfiles();
    }

    private void seedCandidateProfiles() {
        long candidateCount = candidateRepo.count();

        if (candidateCount == 1) {
            log.info("🌱 Seeding 3 additional Candidate Profiles...");

            Role candidateRole = roleRepo.findByName("CANDIDATE")
                    .orElseThrow(() -> new RuntimeException("CANDIDATE role not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(candidateRole);

            // Candidate 1: Junior Java Developer
            Account account1 = Account.builder()
                    .username("nguyenvana")
                    .email("nguyenvana@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .status("ACTIVE")
                    .roles(roles)
                    .build();
            accountRepo.save(account1);

            Candidate candidate1 = Candidate.builder()
                    .account(account1)
                    .fullName("Nguyen Van A")
                    .dob(LocalDate.of(1998, 5, 15))
                    .gender("Male")
                    .phone("0912345678")
                    .address("123 Le Loi Street, District 1, Ho Chi Minh City")
                    .image("https://i.pravatar.cc/150?img=11")
                    .title("Junior Java Developer")
                    .jobLevel("Junior")
                    .experience(2)
                    .link("https://linkedin.com/in/nguyenvana")
                    .build();
            candidateRepo.save(candidate1);
            log.info("✅ Created candidate: {}", candidate1.getFullName());

            // Candidate 2: Senior Frontend Developer
            Account account2 = Account.builder()
                    .username("tranthib")
                    .email("tranthib@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .status("ACTIVE")
                    .roles(roles)
                    .build();
            accountRepo.save(account2);

            Candidate candidate2 = Candidate.builder()
                    .account(account2)
                    .fullName("Tran Thi B")
                    .dob(LocalDate.of(1995, 8, 20))
                    .gender("Female")
                    .phone("0923456789")
                    .address("456 Nguyen Hue Street, District 1, Ho Chi Minh City")
                    .image("https://i.pravatar.cc/150?img=47")
                    .title("Senior Frontend Developer")
                    .jobLevel("Senior")
                    .experience(5)
                    .link("https://github.com/tranthib")
                    .build();
            candidateRepo.save(candidate2);
            log.info("✅ Created candidate: {}", candidate2.getFullName());

            // Candidate 3: Mid-level Full Stack Developer
            Account account3 = Account.builder()
                    .username("levanc")
                    .email("levanc@example.com")
                    .password(passwordEncoder.encode("Password123!"))
                    .status("ACTIVE")
                    .roles(roles)
                    .build();
            accountRepo.save(account3);

            Candidate candidate3 = Candidate.builder()
                    .account(account3)
                    .fullName("Le Van C")
                    .dob(LocalDate.of(1996, 3, 10))
                    .gender("Male")
                    .phone("0934567890")
                    .address("789 Tran Hung Dao Street, District 5, Ho Chi Minh City")
                    .image("https://i.pravatar.cc/150?img=33")
                    .title("Full Stack Developer")
                    .jobLevel("Mid-level")
                    .experience(3)
                    .link("https://portfolio.levanc.com")
                    .build();
            candidateRepo.save(candidate3);
            log.info("✅ Created candidate: {}", candidate3.getFullName());

            log.info("🎉 Successfully seeded 3 candidate profiles!");
        } else {
            log.info("ℹ️ Skipping candidate profile seeding. Current count: {}", candidateCount);
        }
    }
}

