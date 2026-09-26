package rw.afriteck.pms.restaurant.dtos;

import jakarta.validation.constraints.NotBlank;

public record VoidItemRequest(
        @NotBlank String reason
) {
}
