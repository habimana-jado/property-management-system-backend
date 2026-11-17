package rw.afriteck.pms.credit.dtos;

import jakarta.validation.constraints.NotBlank;

public record WriteOffRequest(
        @NotBlank String reason,
        @NotBlank String approvedBy
) {}

