package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Recruiter;
import com.fpt.careermate.services.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.dto.response.NewRecruiterResponse;
import com.fpt.careermate.services.dto.response.RecruiterApprovalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecruiterMapper {
    Recruiter toRecruiter(RecruiterCreationRequest request);

    NewRecruiterResponse toNewRecruiterResponse(Recruiter recruiter);

    @Mapping(source = "recruiter.id", target = "recruiterId")
    @Mapping(source = "recruiter.account.id", target = "accountId")
    @Mapping(source = "recruiter.account.email", target = "email")
    @Mapping(source = "recruiter.account.username", target = "username")
    @Mapping(source = "recruiter.account.status", target = "accountStatus")
    @Mapping(target = "accountRole", ignore = true) // Set manually in service
    RecruiterApprovalResponse toRecruiterApprovalResponse(Recruiter recruiter);
}
