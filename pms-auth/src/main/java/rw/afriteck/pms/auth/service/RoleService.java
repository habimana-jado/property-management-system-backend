package rw.afriteck.pms.auth.service;

import rw.afriteck.pms.auth.dtos.CreateRoleRequest;
import rw.afriteck.pms.auth.dtos.PermissionResponse;
import rw.afriteck.pms.auth.dtos.RoleResponse;
import rw.afriteck.pms.auth.dtos.UpdateRolePermissionsRequest;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRolePermissions(UUID roleId, UpdateRolePermissionsRequest request);

    List<RoleResponse> getAllRoles();

    RoleResponse getRole(UUID roleId);

    void deleteRole(UUID roleId);

    List<PermissionResponse> getAllPermissions();
}
