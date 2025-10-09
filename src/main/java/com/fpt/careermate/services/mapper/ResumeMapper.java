package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.*;
import com.fpt.careermate.services.dto.request.*;
import com.fpt.careermate.services.dto.response.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResumeMapper {

    // Resume mappings
    @Mapping(target = "resumeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "highlightProjects", ignore = true)
    @Mapping(target = "workExperiences", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "foreignLanguages", ignore = true)
    @Mapping(target = "awards", ignore = true)
    Resume toResume(ResumeRequest request);

    @Mapping(target = "candidateId", source = "candidate.candidateId")
    ResumeResponse toResumeResponse(Resume resume);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "resumeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "highlightProjects", ignore = true)
    @Mapping(target = "workExperiences", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "foreignLanguages", ignore = true)
    @Mapping(target = "awards", ignore = true)
    void updateResumeFromRequest(ResumeRequest request, @MappingTarget Resume resume);

    // Certificate mappings
    @Mapping(target = "certificateId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    Certificate toCertificate(CertificateRequest request);
    CertificateResponse toCertificateResponse(Certificate certificate);
    List<Certificate> toCertificateList(List<CertificateRequest> requests);
    List<CertificateResponse> toCertificateResponseList(List<Certificate> certificates);

    // Education mappings
    @Mapping(target = "educationId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    Education toEducation(EducationRequest request);
    EducationResponse toEducationResponse(Education education);
    List<Education> toEducationList(List<EducationRequest> requests);
    List<EducationResponse> toEducationResponseList(List<Education> educations);

    // HighlightProject mappings
    @Mapping(target = "highlightProjectId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    HighlightProject toHighlightProject(HighlightProjectRequest request);
    HighlightProjectResponse toHighlightProjectResponse(HighlightProject highlightProject);
    List<HighlightProject> toHighlightProjectList(List<HighlightProjectRequest> requests);
    List<HighlightProjectResponse> toHighlightProjectResponseList(List<HighlightProject> highlightProjects);

    // WorkExperience mappings
    @Mapping(target = "workExperienceId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    WorkExperience toWorkExperience(WorkExperienceRequest request);
    WorkExperienceResponse toWorkExperienceResponse(WorkExperience workExperience);
    List<WorkExperience> toWorkExperienceList(List<WorkExperienceRequest> requests);
    List<WorkExperienceResponse> toWorkExperienceResponseList(List<WorkExperience> workExperiences);

    // Skill mappings
    @Mapping(target = "skillId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    Skill toSkill(SkillRequest request);
    SkillResponse toSkillResponse(Skill skill);
    List<Skill> toSkillList(List<SkillRequest> requests);
    List<SkillResponse> toSkillResponseList(List<Skill> skills);

    // ForeignLanguage mappings
    @Mapping(target = "foreignLanguageId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    ForeignLanguage toForeignLanguage(ForeignLanguageRequest request);
    ForeignLanguageResponse toForeignLanguageResponse(ForeignLanguage foreignLanguage);
    List<ForeignLanguage> toForeignLanguageList(List<ForeignLanguageRequest> requests);
    List<ForeignLanguageResponse> toForeignLanguageResponseList(List<ForeignLanguage> foreignLanguages);

    // Award mappings
    @Mapping(target = "awardId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    Award toAward(AwardRequest request);
    AwardResponse toAwardResponse(Award award);
    List<Award> toAwardList(List<AwardRequest> requests);
    List<AwardResponse> toAwardResponseList(List<Award> awards);
}
