package rw.afriteck.pms.restaurant.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateMenuCategoryRequest(
        @NotBlank String name,
        @NotNull UUID restaurantId) {}
