package rw.afriteck.pms.pos.service;

import rw.afriteck.pms.pos.dtos.*;

import java.util.List;
import java.util.UUID;

public interface POSTerminalService {

    POSTerminalResponse create(CreateTerminalRequest request);

    POSTerminalResponse findById(UUID id);

    List<POSTerminalResponse> findByHotelBranch(UUID hotelBranchId);

    POSTerminalResponse activate(UUID id);

    POSTerminalResponse deactivate(UUID id);
}
