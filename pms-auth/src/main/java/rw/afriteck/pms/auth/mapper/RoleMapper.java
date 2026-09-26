package rw.afriteck.pms.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.auth.dtos.PermissionResponse;
import rw.afriteck.pms.auth.dtos.RoleResponse;
import rw.afriteck.pms.auth.model.Permission;
import rw.afriteck.pms.auth.model.Role;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RoleMapper {
    PermissionResponse toResponse(Permission permission);
    RoleResponse toResponse(Role role);
}
