package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JdSkillResponse {

    int id;
    String name;
    String type;

}
