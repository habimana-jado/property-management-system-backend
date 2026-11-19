package rw.afriteck.pms.common.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

/** For tenant-owned entities that have no audit columns yet. */
@MappedSuperclass
@Getter   // no setter on purpose: Hibernate fills it from the resolver
public abstract class TenantOwned {

    @TenantId
    @Column(name = "hotel_id", nullable = false, updatable = false)
    private UUID hotelId;
}

