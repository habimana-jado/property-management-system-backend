package rw.afriteck.pms.stock.api.setup;

import java.util.UUID;

public record CategoryView(UUID id, String code, String name, UUID parentId, boolean active) {}

