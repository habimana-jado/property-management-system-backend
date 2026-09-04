package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTableMasterRequest(
        @NotBlank String tableNumber,
        @NotNull int tableCapacity,
        @NotNull UUID restaurantId
) {}
