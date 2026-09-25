package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.dtos.StaffResponse;
import rw.afriteck.pms.model.Role;
import rw.afriteck.pms.model.Staff;
import rw.afriteck.pms.model.User;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StaffMapper {

    @Mapping(source = "staff.id", target = "id")
    @Mapping(source = "staff.hotelBranch.id", target = "hotelBranchId")
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
