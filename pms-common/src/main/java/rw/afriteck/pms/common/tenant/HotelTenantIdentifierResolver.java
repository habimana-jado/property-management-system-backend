package rw.afriteck.pms.common.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import java.util.UUID;

public class HotelTenantIdentifierResolver implements CurrentTenantIdentifierResolver<UUID> {

    @Override
    public UUID resolveCurrentTenantIdentifier() {
        return TenantContext.get().orElse(TenantContext.NO_TENANT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true; // changing the tenant mid-request fails loudly instead of leaking
    }
}
