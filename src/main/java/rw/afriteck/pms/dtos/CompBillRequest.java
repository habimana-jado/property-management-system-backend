package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotBlank;

public record CompBillRequest(
        @NotBlank String reason,
        @NotBlank String authorizedBy
) {
}
