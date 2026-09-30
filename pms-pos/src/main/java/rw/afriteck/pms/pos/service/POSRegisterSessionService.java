package rw.afriteck.pms.pos.service;

import rw.afriteck.pms.pos.dtos.CloseRegisterSessionRequest;
import rw.afriteck.pms.pos.dtos.OpenRegisterSessionRequest;
import rw.afriteck.pms.pos.dtos.POSRegisterSessionResponse;

import java.util.UUID;

public interface POSRegisterSessionService {

    POSRegisterSessionResponse open(OpenRegisterSessionRequest request);

    POSRegisterSessionResponse close(UUID id, CloseRegisterSessionRequest request);

    POSRegisterSessionResponse findById(UUID id);

    POSRegisterSessionResponse getActiveSession(UUID terminalId);
}
