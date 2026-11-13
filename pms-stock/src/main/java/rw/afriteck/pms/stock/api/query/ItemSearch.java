package rw.afriteck.pms.stock.api.query;

import rw.afriteck.pms.stock.api.enums.EItemType;

import java.util.UUID;

public record ItemSearch(String text, UUID categoryId, EItemType type, boolean activeOnly) {}

