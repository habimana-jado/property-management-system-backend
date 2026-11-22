package rw.afriteck.pms.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.CreateRoleRequest;
import rw.afriteck.pms.auth.dtos.PermissionResponse;
import rw.afriteck.pms.auth.dtos.RoleResponse;
import rw.afriteck.pms.auth.dtos.UpdateRolePermissionsRequest;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.DuplicateResourceException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.auth.mapper.RoleMapper;
import rw.afriteck.pms.auth.model.Permission;
import rw.afriteck.pms.auth.model.Role;
import rw.afriteck.pms.auth.repository.PermissionRepo;
import rw.afriteck.pms.auth.repository.RoleRepo;
import rw.afriteck.pms.auth.service.RoleService;
import rw.afriteck.pms.common.tenant.TenantContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepo roleRepository;
    private final PermissionRepo permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        UUID hotelId = TenantContext.require();
        if (roleRepository.existsByHotelIdAndName(hotelId, request.name())) {
            throw new DuplicateResourceException("Role already exists: " + request.name());
        }

        Role role = new Role();
        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(resolvePermissions(request.permissionCodes()));
        role.setHotelId(hotelId);

        Role saved = roleRepository.save(role);
        return roleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public RoleResponse updateRolePermissions(UUID roleId, UpdateRolePermissionsRequest request) {
        Role role = findRoleOrThrow(roleId);
        role.setPermissions(resolvePermissions(request.permissionCodes()));
        // dirty checking persists the change - no explicit save() needed
        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findByHotelId(TenantContext.require()).stream().map(roleMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRole(UUID roleId) {
        return roleMapper.toResponse(findRoleOrThrow(roleId));
    }

    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = findRoleOrThrow(roleId);
        if (role.isSystemDefined()) {
            throw new BusinessRuleViolationException("UNAUTHORIZED_DELETE","Cannot delete a system-defined role: " + role.getName());
        }
        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream().map(roleMapper::toResponse).toList();
    }

    private Role findRoleOrThrow(UUID roleId) {
        return roleRepository.findByIdAndHotelId(roleId, TenantContext.require())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found", roleId));
    }

    private Set<Permission> resolvePermissions(Set<String> codes) {
        if (codes.stream().anyMatch(code -> code.startsWith("PLATFORM_"))) {
            throw new BusinessRuleViolationException("INVALID_PERMISSION",
                    "Platform permissions cannot be assigned to hotel roles");
        }
        List<Permission> found = permissionRepository.findByCodeIn(codes);
        if (found.size() != codes.size()) {
            throw new ResourceNotFoundException("One or more permission codes are invalid", codes);
        }
        return new HashSet<>(found);
    }
}
