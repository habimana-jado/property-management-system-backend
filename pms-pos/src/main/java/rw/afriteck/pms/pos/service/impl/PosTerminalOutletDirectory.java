package rw.afriteck.pms.pos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.pos.model.POSTerminal;
import rw.afriteck.pms.pos.repository.POSTerminalRepository;
import rw.afriteck.pms.stock.api.OutletDirectory;
import rw.afriteck.pms.stock.api.enums.EOutletType;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class PosTerminalOutletDirectory implements OutletDirectory {
    private final POSTerminalRepository terminals;

    @Override public EOutletType type() { return EOutletType.POS_TERMINAL; }
    @Override public Optional<UUID> branchIdOf(UUID outletId) {
        return terminals.findById(outletId).map(POSTerminal::getHotelBranchId);
    }
}
