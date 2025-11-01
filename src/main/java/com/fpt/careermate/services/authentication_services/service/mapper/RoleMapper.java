package com.fpt.careermate.services.authentication_services.service.mapper;

import com.fpt.careermate.services.authentication_services.domain.Role;
import com.fpt.careermate.services.authentication_services.service.dto.request.RoleRequest;
import com.fpt.careermate.services.authentication_services.service.dto.response.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    @Mapping(target = "name", qualifiedByName = "removeRolePrefix")
    RoleResponse toRoleResponse(Role role);

    @Named("removeRolePrefix")
    default String removeRolePrefix(String roleName) {
        if (roleName != null && roleName.startsWith("ROLE_")) {
            return roleName.substring(5); // Remove "ROLE_" prefix
        }
        return roleName;
    }
}
