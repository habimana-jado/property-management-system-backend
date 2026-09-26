package rw.afriteck.pms.restaurant.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTableMasterRequest(
        @NotBlank String tableNumber,
        @NotNull int tableCapacity,
        @NotNull UUID restaurantId
) {}
