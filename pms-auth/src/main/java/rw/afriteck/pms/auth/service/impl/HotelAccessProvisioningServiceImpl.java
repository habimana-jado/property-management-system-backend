package rw.afriteck.pms.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.ProvisionAdminRequest;
import rw.afriteck.pms.auth.dtos.ProvisionedAdmin;
import rw.afriteck.pms.auth.helpers.LoginNames;
import rw.afriteck.pms.auth.model.Role;
import rw.afriteck.pms.auth.model.Staff;
import rw.afriteck.pms.auth.model.User;
import rw.afriteck.pms.auth.repository.PermissionRepo;
import rw.afriteck.pms.auth.repository.RoleRepo;
import rw.afriteck.pms.auth.repository.StaffRepo;
import rw.afriteck.pms.auth.repository.UserRepo;
import rw.afriteck.pms.auth.service.HotelAccessProvisioningService;
import rw.afriteck.pms.common.exception.DuplicateResourceException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelAccessProvisioningServiceImpl implements HotelAccessProvisioningService {

    private final RoleRepo roleRepository;
    private final PermissionRepo permissionRepository;
    private final StaffRepo staffRepository;
    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ProvisionedAdmin provisionHotelAdmin(UUID hotelId, String hotelCode, ProvisionAdminRequest request){
        String loginName = LoginNames.compose(hotelCode, request.username());
        if (userRepository.findByUsername(loginName).isPresent()) {
            throw new DuplicateResourceException("Username already taken: " + loginName);
        }

        Role admin = new Role();
        admin.setHotelId(hotelId);
        admin.setName("ADMIN");
        admin.setDescription("Hotel administrator");
        admin.setSystemDefined(true);
        admin.setPermissions(new HashSet<>(permissionRepository.findAll())); // platform codes never live here
        Role savedRole = roleRepository.save(admin);

        Staff staff = new Staff();
        staff.setFirstName(request.firstName());
        staff.setLastName(request.lastName());
        staff.setEmail(request.email());
        staff.setPosition("Administrator");
        staff.setHireDate(LocalDate.now());
        staff.setHotelId(hotelId);               // explicit: the operator has no tenant context
        Staff savedStaff = staffRepository.save(staff);

        User user = new User();
        user.setStaff(savedStaff);
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(new HashSet<>(Set.of(savedRole)));
        User savedUser = userRepository.save(user);

        return new ProvisionedAdmin(savedStaff.getId(), savedUser.getId(), savedRole.getId(), loginName);
    }
}