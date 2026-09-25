package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.dtos.PermissionResponse;
import rw.afriteck.pms.dtos.RoleResponse;
import rw.afriteck.pms.model.Permission;
import rw.afriteck.pms.model.Role;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RoleMapper {
    PermissionResponse toResponse(Permission permission);
    RoleResponse toResponse(Role role);
}
