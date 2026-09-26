package rw.afriteck.pms.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.auth.dtos.StaffResponse;
import rw.afriteck.pms.auth.model.Role;
import rw.afriteck.pms.auth.model.Staff;
import rw.afriteck.pms.auth.model.User;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StaffMapper {

    @Mapping(source = "staff.id", target = "id")
    @Mapping(source = "staff.hotelBranchId", target = "hotelBranchId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.enabled", target = "accountEnabled")
    @Mapping(target = "roleNames", expression = "java(mapRoleNames(user))")
    StaffResponse toResponse(Staff staff, User user);

    default List<String> mapRoleNames(User user) {
        if (user == null) {
            return List.of();
        }
        return user.getRoles().stream().map(Role::getName).toList();
    }
}
