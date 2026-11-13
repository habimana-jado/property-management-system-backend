package rw.afriteck.pms.stock.api.setup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoryUpdateCommand(@NotBlank @Size(max = 200) String name, UUID parentId) {}

