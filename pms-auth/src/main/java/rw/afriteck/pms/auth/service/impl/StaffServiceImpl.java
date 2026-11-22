package rw.afriteck.pms.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.StaffRegistrationRequest;
import rw.afriteck.pms.auth.dtos.StaffResponse;
import rw.afriteck.pms.auth.dtos.TemporaryPasswordResponse;
import rw.afriteck.pms.auth.dtos.UpdateStaffProfileRequest;
import rw.afriteck.pms.auth.helpers.LoginNames;
import rw.afriteck.pms.auth.helpers.PasswordResetRules;
import rw.afriteck.pms.auth.helpers.TemporaryPasswordIssuer;
import rw.afriteck.pms.auth.model.Permission;
import rw.afriteck.pms.auth.security.StaffPrincipal;
import rw.afriteck.pms.common.exception.DuplicateResourceException;
import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.auth.mapper.StaffMapper;
import rw.afriteck.pms.auth.model.Role;
import rw.afriteck.pms.auth.model.Staff;
import rw.afriteck.pms.auth.model.User;
import rw.afriteck.pms.auth.repository.RoleRepo;
import rw.afriteck.pms.auth.repository.StaffRepo;
import rw.afriteck.pms.auth.repository.UserRepo;
import rw.afriteck.pms.auth.service.StaffService;
import rw.afriteck.pms.common.security.BranchAccessGuard;
import rw.afriteck.pms.common.tenant.HotelDirectory;
import rw.afriteck.pms.common.tenant.TenantContext;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepo staffRepository;
    private final UserRepo userRepository;
    private final RoleRepo roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffMapper staffMapper;
    private final HotelDirectory hotelDirectory;
    private final TemporaryPasswordIssuer passwordIssuer;
    private final BranchAccessGuard branchAccess;

    @Override
    @Transactional
    public StaffResponse registerStaff(StaffRegistrationRequest request) {
        UUID hotelId = TenantContext.require();
        requireBranchInHotel(request.hotelBranchId(), hotelId);

        String loginName = LoginNames.compose(hotelDirectory.hotelCodeOf(hotelId), request.username());
        if (userRepository.findByUsername(loginName).isPresent()) {
            throw new DuplicateResourceException("Username already taken: " + request.username());
        }
        Set<Role> roles = resolveRoles(request.roleIds(), hotelId);

        Staff staff = new Staff();
        staff.setFirstName(request.firstName());
        staff.setLastName(request.lastName());
        staff.setContactNumber(request.contactNumber());
        staff.setEmail(request.email());
        staff.setPosition(request.position());
        staff.setHireDate(request.hireDate() != null ? request.hireDate() : LocalDate.now());
        staff.setHotelId(hotelId);
        staff.setHotelBranchId(request.hotelBranchId());

        Staff savedStaff = staffRepository.save(staff);

        User user = new User();
        user.setStaff(savedStaff);
        user.setUsername(loginName);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setMustChangePassword(true);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        return staffMapper.toResponse(savedStaff, savedUser);
    }

    @Override
    @Transactional
    public StaffResponse updateProfile(UUID staffId, UpdateStaffProfileRequest request) {
        if (request.hotelBranchId() != null) {
            requireBranchInHotel(request.hotelBranchId(), TenantContext.require());
        }

        Staff staff = findStaffOrThrow(staffId);

        staff.setFirstName(request.firstName());
        staff.setLastName(request.lastName());
        staff.setContactNumber(request.contactNumber());
        staff.setEmail(request.email());
        staff.setPosition(request.position());
        staff.setHireDate(request.hireDate());
        staff.setHotelBranchId(request.hotelBranchId());
        // dirty checking persists the change - no explicit save() needed

        User user = userRepository.findByStaffId(staffId).orElse(null);
        return staffMapper.toResponse(staff, user);
    }

    @Override
    @Transactional
    public TemporaryPasswordResponse resetPassword(UUID staffId) {
        Staff staff = findStaffOrThrow(staffId);                      // tenant-scoped lookup
        branchAccess.requireAccessTo(staff.getHotelBranchId());       // branch staff only reach their own branch

        User target = userRepository.findByStaffId(staff.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for staff", staffId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof StaffPrincipal requester)) {
            throw new ForbiddenException("STAFF_ONLY", "Only hotel staff can reset passwords here");
        }
        Set<String> requesterAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        Set<String> targetPermissions = target.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode).collect(Collectors.toSet());

        PasswordResetRules.requireAllowed(requester.userId(), target.getId(), requesterAuthorities, targetPermissions);

        String temporary = passwordIssuer.issueFor(target);
//        log.info("Password reset for {} by {}", target.getUsername(), requester.username());   // never log the password
        return new TemporaryPasswordResponse(target.getUsername(), temporary);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaff(UUID staffId) {
        Staff staff = findStaffOrThrow(staffId);
        User user = userRepository.findByStaffId(staffId).orElseThrow(() -> new ResourceNotFoundException("User", staffId));
        return staffMapper.toResponse(staff, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> findByHotelBranch(UUID hotelBranchId, Pageable pageable) {
        Page<Staff> staffList = staffRepository.findByHotelIdAndHotelBranchId(
                TenantContext.require(), hotelBranchId, pageable);

        if (staffList.isEmpty()) {
            return List.of();
        }

        List<UUID> staffIds = staffList.stream().map(Staff::getId).toList();
        Map<UUID, User> usersByStaffId = userRepository.findByStaffIdIn(staffIds).stream()
                .collect(Collectors.toMap(u -> u.getStaff().getId(), u -> u));

        return staffList.stream()
                .map(staff -> staffMapper.toResponse(staff, usersByStaffId.get(staff.getId())))
                .toList();
    }

    private void requireBranchInHotel(UUID branchId, UUID hotelId) {
        if (!hotelDirectory.branchBelongsToHotel(branchId, hotelId)) {
            throw new ResourceNotFoundException("Hotel Branch", branchId);
        }
    }

    private Staff findStaffOrThrow(UUID staffId) {
        return staffRepository.findByIdAndHotelId(staffId, TenantContext.require())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found", staffId));
    }

    private Set<Role> resolveRoles(Set<UUID> roleIds, UUID hotelId) {
        List<Role> found = roleRepository.findAllByIdInAndHotelId(roleIds, hotelId);
        if (found.size() != roleIds.size()) {
            throw new ResourceNotFoundException("One or more role IDs are invalid", roleIds);
        }
        return new HashSet<>(found);
    }
}
