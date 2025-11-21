package com.fpt.careermate.services.resume_services.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.resume_services.domain.HighlightProject;
import com.fpt.careermate.services.resume_services.domain.Resume;
import com.fpt.careermate.services.resume_services.repository.HighlightProjectRepo;
import com.fpt.careermate.services.resume_services.repository.ResumeRepo;
import com.fpt.careermate.services.resume_services.service.dto.request.HighlightProjectRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.HighlightProjectResponse;
import com.fpt.careermate.services.resume_services.service.impl.HighLightProjectService;
import com.fpt.careermate.services.resume_services.service.mapper.HighlightProjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class HighLightProjectImp implements HighLightProjectService {
    HighlightProjectRepo highlightProjectRepo;
    ResumeImp resumeImp;
    HighlightProjectMapper highlightProjectMapper;
    ResumeRepo resumeRepo;

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public HighlightProjectResponse addHighlightProjectToResume(HighlightProjectRequest highlightProject) {
        Resume resume = resumeImp.getResumeEntityById(highlightProject.getResumeId());

        if (highlightProjectRepo.countHighlightProjectByResumeId(resume.getResumeId()) >= 3) {
            throw new AppException(ErrorCode.OVERLOAD);
        }

        HighlightProject highlightProjectInfo = highlightProjectMapper.toEntity(highlightProject);
        highlightProjectInfo.setResume(resume);
        resume.getHighlightProjects().add(highlightProjectInfo);

        HighlightProject savedHighlightProject = highlightProjectRepo.save(highlightProjectInfo);

        return highlightProjectMapper.toResponse(savedHighlightProject);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public void removeHighlightProjectFromResume(int highlightProjectId) {
        highlightProjectRepo.findById(highlightProjectId)
                .orElseThrow(() -> new AppException(ErrorCode.HIGHLIGHT_PROJECT_NOT_FOUND));
        highlightProjectRepo.deleteById(highlightProjectId);
    }

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public HighlightProjectResponse updateHighlightProjectInResume(int resumeId, int highlightProjectId, HighlightProjectRequest highlightProject) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        HighlightProject existingHighlightProject = highlightProjectRepo.findById(highlightProjectId)
                .orElseThrow(() -> new AppException(ErrorCode.HIGHLIGHT_PROJECT_NOT_FOUND));

        highlightProjectMapper.updateEntity(highlightProject, existingHighlightProject);

        return highlightProjectMapper.toResponse(highlightProjectRepo.save(existingHighlightProject));
    }
}
