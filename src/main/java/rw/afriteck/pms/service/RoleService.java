package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.CreateRoleRequest;
import rw.afriteck.pms.dtos.PermissionResponse;
import rw.afriteck.pms.dtos.RoleResponse;
import rw.afriteck.pms.dtos.UpdateRolePermissionsRequest;

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
