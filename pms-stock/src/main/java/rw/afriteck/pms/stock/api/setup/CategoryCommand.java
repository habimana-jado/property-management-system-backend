package rw.afriteck.pms.stock.api.setup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoryCommand(@NotBlank @Size(max = 50) String code, @NotBlank @Size(max = 200) String name, UUID parentId) {}

