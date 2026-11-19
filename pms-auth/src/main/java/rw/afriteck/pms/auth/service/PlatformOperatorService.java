package rw.afriteck.pms.auth.service;

import rw.afriteck.pms.auth.dtos.CreatePlatformOperatorRequest;
import rw.afriteck.pms.auth.dtos.PlatformOperatorResponse;

import java.util.List;
import java.util.UUID;

public interface PlatformOperatorService {
    PlatformOperatorResponse create(CreatePlatformOperatorRequest request);

    PlatformOperatorResponse setEnabled(UUID operatorId, boolean enabled);

    List<PlatformOperatorResponse> findAll();
}
