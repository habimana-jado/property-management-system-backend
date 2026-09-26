package rw.afriteck.pms.auth.dtos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StaffResponse(
        UUID id,
        String firstName,
        String lastName,
        String contactNumber,
        String email,
        String position,
        LocalDate hireDate,
        UUID hotelBranchId,
        UUID userId,
        String username,
        Boolean accountEnabled,
        List<String> roleNames
) {
}
