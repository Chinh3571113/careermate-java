package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Recruiter;
import com.fpt.careermate.services.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.dto.response.NewRecruiterResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecruiterMapper {
    Recruiter toRecruiter(RecruiterCreationRequest request);

    NewRecruiterResponse toNewRecruiterResponse(Recruiter recruiter);
}
