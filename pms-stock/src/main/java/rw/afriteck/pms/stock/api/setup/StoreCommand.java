package rw.afriteck.pms.stock.api.setup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rw.afriteck.pms.stock.api.enums.EStoreType;

public record StoreCommand(@NotBlank @Size(max = 50) String code,
                           @NotBlank @Size(max = 200) String name, @NotNull EStoreType storeType) {}