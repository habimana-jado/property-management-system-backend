package rw.afriteck.pms.platform.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import rw.afriteck.pms.auth.dtos.ProvisionAdminRequest;
import rw.afriteck.pms.property.dtos.CreateHotelRequest;

public record OnboardHotelRequest(
        @Valid @NotNull CreateHotelRequest hotel,
        @Valid @NotNull ProvisionAdminRequest admin) {}

