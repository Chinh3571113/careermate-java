package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.services.job_services.domain.JdSkill;
import com.fpt.careermate.services.job_services.repository.JobDescriptionRepo;
import com.fpt.careermate.services.job_services.repository.JdSkillRepo;
import com.fpt.careermate.services.job_services.service.dto.response.JdSkillResponse;
import com.fpt.careermate.services.job_services.service.impl.JdSkillService;
import com.fpt.careermate.services.job_services.service.mapper.JdSkillMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @Override
    public List<JdSkillResponse> getAllSkill() {
        return jdSkillMapper.toSetSkillResponse(jdSkillRepo.findAll());
    }

    // Kiểm tra 50 record đầu trong JobDescription entity có JdSkill nào được sử dụng nhiều nhất gọm lại thành 1 list rồi trả về
    @Override
    public List<JdSkillResponse> getTopUsedSkillsFromFirst50() {
        // Lấy dữ liệu từ query: [skill_id, count]
        List<Object[]> topSkillsData = jobDescriptionRepo.findTopSkillsFromFirst50Records();

        // Lấy danh sách skill IDs theo thứ tự usage count giảm dần
        List<Integer> skillIds = topSkillsData.stream()
                .map(row -> ((Number) row[0]).intValue())
                .collect(Collectors.toList());

        // Fetch JdSkill entities và giữ nguyên thứ tự (nhiều nhất -> ít nhất)
        List<JdSkill> topSkills = jdSkillRepo.findAllById(skillIds).stream()
                .sorted((s1, s2) -> Integer.compare(
                        skillIds.indexOf(s1.getId()),
                        skillIds.indexOf(s2.getId())))
                .collect(Collectors.toList());

        // Convert sang DTO và trả về
        return jdSkillMapper.toSetSkillResponse(topSkills);
    }


}
