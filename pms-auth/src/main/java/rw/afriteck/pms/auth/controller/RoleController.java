package rw.afriteck.pms.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.auth.dtos.CreateRoleRequest;
import rw.afriteck.pms.auth.dtos.PermissionResponse;
import rw.afriteck.pms.auth.dtos.RoleResponse;
import rw.afriteck.pms.auth.dtos.UpdateRolePermissionsRequest;
import rw.afriteck.pms.auth.service.RoleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public RoleResponse createRole(@Valid @RequestBody CreateRoleRequest request) {
        return roleService.createRole(request);
    }

    @PatchMapping("/{roleId}/permissions")
    public RoleResponse updatePermissions(@PathVariable UUID roleId,
                                           @Valid @RequestBody UpdateRolePermissionsRequest request) {
        return roleService.updateRolePermissions(roleId, request);
    }

    @GetMapping
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    @GetMapping("/{roleId}")
    public RoleResponse getRole(@PathVariable UUID roleId) {
        return roleService.getRole(roleId);
    }

    @DeleteMapping("/{roleId}")
    public void deleteRole(@PathVariable UUID roleId) {
        roleService.deleteRole(roleId);
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> getAllPermissions() {
        return roleService.getAllPermissions();
    }
}
