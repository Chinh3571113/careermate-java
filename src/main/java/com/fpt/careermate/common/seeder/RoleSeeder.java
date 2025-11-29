package com.fpt.careermate.common.seeder;

import com.fpt.careermate.services.authentication_services.domain.Role;
import com.fpt.careermate.services.authentication_services.repository.RoleRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 🌱 RoleSeeder
 *
 * Seed dữ liệu cho roles
 * - Khởi tạo các roles cần thiết khi app chạy (CANDIDATE, RECRUITER, ADMIN)
 * - Chạy đầu tiên (Order 0) để đảm bảo roles đã tồn tại trước khi các seeders khác chạy
 */
@Component
@Order(0) // Chạy đầu tiên trước tất cả seeders khác
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleSeeder implements CommandLineRunner {

    RoleRepo roleRepo;

    @Override
    public void run(String... args) throws Exception {
        log.info("🌱 Checking and seeding roles...");

        List<RoleData> rolesToSeed = Arrays.asList(
            new RoleData("CANDIDATE", "Role for job seekers and candidates"),
            new RoleData("RECRUITER", "Role for company recruiters and HR managers"),
            new RoleData("ADMIN", "Role for system administrators")
        );

        int createdCount = 0;
        int existingCount = 0;

        for (RoleData roleData : rolesToSeed) {
            if (roleRepo.findByName(roleData.name).isEmpty()) {
                Role role = Role.builder()
                        .name(roleData.name)
                        .description(roleData.description)
                        .build();

                roleRepo.save(role);
                log.info("✅ Created role: {} - {}", role.getName(), role.getDescription());
                createdCount++;
            } else {
                log.debug("ℹ️ Role '{}' already exists", roleData.name);
                existingCount++;
            }
        }

        if (createdCount > 0) {
            log.info("🎉 Successfully seeded {} new roles!", createdCount);
        }
        if (existingCount > 0) {
            log.info("ℹ️ {} roles already existed", existingCount);
        }
    }

    /**
     * Helper class để lưu thông tin role
     */
    private static class RoleData {
        String name;
        String description;

        RoleData(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}

