package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotBlank;

public record VoidItemRequest(
        @NotBlank String reason
) {
}
