package com.fpt.careermate.services.account_services.service.dto.response;

import com.fpt.careermate.services.authentication_services.service.dto.response.RoleResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {
    int id;
    String username;
    String email;
    String status;
    Set<RoleResponse> roles;

}
