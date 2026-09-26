package rw.afriteck.pms.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.StaffRegistrationRequest;
import rw.afriteck.pms.auth.dtos.StaffResponse;
import rw.afriteck.pms.auth.dtos.UpdateStaffProfileRequest;
import rw.afriteck.pms.common.exception.DuplicateResourceException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.auth.mapper.StaffMapper;
import rw.afriteck.pms.auth.model.Role;
import rw.afriteck.pms.auth.model.Staff;
import rw.afriteck.pms.auth.model.User;
import rw.afriteck.pms.auth.repository.RoleRepo;
import rw.afriteck.pms.auth.repository.StaffRepo;
import rw.afriteck.pms.auth.repository.UserRepo;
import rw.afriteck.pms.auth.service.StaffService;

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

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('STAFF_MANAGE')")
    public StaffResponse registerStaff(StaffRegistrationRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new DuplicateResourceException("Username already taken: " + request.username());
        }
        Set<Role> roles = resolveRoles(request.roleIds());

        Staff staff = new Staff();
        staff.setFirstName(request.firstName());
        staff.setLastName(request.lastName());
        staff.setContactNumber(request.contactNumber());
        staff.setEmail(request.email());
        staff.setPosition(request.position());
        staff.setHireDate(request.hireDate() != null ? request.hireDate() : LocalDate.now());
        staff.setHotelBranchId(request.hotelBranchId());

        Staff savedStaff = staffRepository.save(staff);

        User user = new User();
        user.setStaff(savedStaff);
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        return staffMapper.toResponse(savedStaff, savedUser);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('STAFF_MANAGE')")
    public StaffResponse updateProfile(UUID staffId, UpdateStaffProfileRequest request) {
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
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('STAFF_MANAGE')")
    public StaffResponse getStaff(UUID staffId) {
        Staff staff = findStaffOrThrow(staffId);
        User user = userRepository.findByStaffId(staffId).orElseThrow(() -> new ResourceNotFoundException("User", staffId));
        return staffMapper.toResponse(staff, user);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('STAFF_MANAGE')")
    public List<StaffResponse> findByHotelBranch(UUID hotelBranchId, Pageable pageable) {
        Page<Staff> staffList = staffRepository.findByHotelBranchId(hotelBranchId, pageable);
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

    private Staff findStaffOrThrow(UUID staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found", staffId));
    }

    private Set<Role> resolveRoles(Set<UUID> roleIds) {
        List<Role> found = roleRepository.findAllById(roleIds);
        if (found.size() != roleIds.size()) {
            throw new ResourceNotFoundException("One or more role IDs are invalid", roleIds);
        }
        return new HashSet<>(found);
    }
}
