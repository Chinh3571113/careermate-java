package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.services.job_services.domain.JdSkill;
import com.fpt.careermate.services.job_services.repository.JobDescriptionRepo;
import com.fpt.careermate.services.job_services.repository.JdSkillRepo;
import com.fpt.careermate.services.job_services.service.dto.response.JdSkillResponse;
import com.fpt.careermate.services.job_services.service.impl.JdSkillService;
import com.fpt.careermate.services.job_services.service.mapper.JdSkillMapper;
import com.fpt.careermate.common.constant.StatusJdSkill;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JdJdSkillImp implements JdSkillService {

    JdSkillRepo jdSkillRepo;
    JdSkillMapper jdSkillMapper;
    JobDescriptionRepo jobDescriptionRepo;

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void createSkill(String name) {
        // Check jdSkill name
        Optional<JdSkill> exSkill = jdSkillRepo.findSkillByName(name);
        if (exSkill.isPresent()) throw new AppException(ErrorCode.SKILL_EXISTED);

        JdSkill jdSkill = new JdSkill();
        jdSkill.setName(name);
        jdSkillRepo.save(jdSkill);
    }

    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN', 'CANDIDATE')")
    @Override
    public List<JdSkillResponse> getAllSkill(String keyword, String type) {
        // Validate and normalize type into an effectively final variable
        final String normalizedType = validateAndNormalizeType(type);

        // If keyword is null or empty, return all skills (optionally filtered by type)
        List<JdSkill> skills = (keyword == null || keyword.trim().isEmpty())
                ? jdSkillRepo.findAll()
                : jdSkillRepo.searchByKeyword(keyword.trim());

        // Apply type filter in-memory if requested
        if (normalizedType != null) {
            skills = skills.stream()
                    .filter(s -> s.getType() != null && s.getType().trim().equalsIgnoreCase(normalizedType))
                    .toList();
        }

        return jdSkillMapper.toSetSkillResponse(skills);
    }

    // Helper to validate and normalize the 'type' request param
    private String validateAndNormalizeType(String type) {
        if (type == null) return null;
        String normalized = type.trim().toLowerCase();
        if (normalized.isEmpty()) return null;
        if (!StatusJdSkill.CORE.equalsIgnoreCase(normalized) && !StatusJdSkill.SOFT.equalsIgnoreCase(normalized)) {
            throw new AppException(ErrorCode.INVALID_JDSKILL_TYPE);
        }
        return normalized;
    }

    // Kiểm tra 50 record đầu trong JobDescription entity có JdSkill nào được sử dụng nhiều nhất gọm lại thành 1 list rồi trả về
    @Override
    public List<JdSkillResponse> getTopUsedSkillsFromFirst50() {
        // Lấy dữ liệu từ query: [skill_id, count]
        List<Object[]> topSkillsData = jobDescriptionRepo.findTopSkillsFromFirst50Records();

        // Lấy danh sách skill IDs theo thứ tự usage count giảm dần
        List<Integer> skillIds = topSkillsData.stream()
                .map(row -> ((Number) row[0]).intValue())
                .toList();

        // Fetch JdSkill entities và giữ nguyên thứ tự (nhiều nhất -> ít nhất)
        List<JdSkill> topSkills = jdSkillRepo.findAllById(skillIds).stream()
                .sorted(Comparator.comparingInt(s -> skillIds.indexOf(s.getId())))
                .toList();

        // Convert sang DTO và trả về
        return jdSkillMapper.toSetSkillResponse(topSkills);
    }


}
