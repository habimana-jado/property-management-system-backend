package rw.afriteck.pms.stock.api.setup;

import rw.afriteck.pms.stock.api.enums.EItemType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ItemView(UUID id, String code, String name, String description, UUID categoryId, String categoryName,
                       UUID baseUnitId, String baseUnitCode, EItemType itemType, BigDecimal reorderLevel,
                       boolean active, List<PackagingView> packagings) {}
