package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MergeTableRequest(
        @NotNull UUID sourceTableId
) {
}
