package rw.afriteck.pms.stock.api.setup;

import rw.afriteck.pms.stock.api.enums.EUnitGroup;

import java.math.BigDecimal;
import java.util.UUID;

public record UnitView(UUID id, String code, String name, EUnitGroup unitGroup, BigDecimal factorToGroupBase) {}
