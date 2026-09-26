package rw.afriteck.pms.restaurant.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MergeTableRequest(
        @NotNull UUID sourceTableId
) {
}
