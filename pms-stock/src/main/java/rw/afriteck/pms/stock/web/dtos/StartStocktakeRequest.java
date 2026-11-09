package rw.afriteck.pms.stock.web.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartStocktakeRequest(@NotNull UUID storeId) {
}
