package rw.afriteck.pms.common.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.TenantId;
import rw.afriteck.pms.common.audit.Auditable;

import java.util.UUID; /** For tenant-owned entities that already extend Auditable (POSSale, POSRegisterSession, ...). */
@MappedSuperclass
@Getter
public abstract class AuditableTenantOwned extends Auditable {

    @TenantId
    @Column(name = "hotel_id", nullable = false, updatable = false)
    private UUID hotelId;
}
