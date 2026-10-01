package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.stock.api.enums.EOutletType;

import java.util.UUID;

@Entity
@Table(name = "stock_store_assignment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"store_id", "outlet_type", "outlet_id"}),
                @UniqueConstraint(columnNames = {"default_key"})
        })
@Getter
@Setter
public class StoreAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "outlet_type", nullable = false, length = 30)
    private EOutletType outletType;

    @Column(name = "outlet_id", nullable = false)
    private UUID outletId; // plain id: no dependency on outlet modules

    private boolean isDefault;

    private boolean active = true;

    // "<outletType>:<outletId>" only on the active default assignment:
    // unique constraint => one default issuing store per outlet.
    @Column(name = "default_key", length = 80)
    private String defaultKey;

    @PrePersist
    @PreUpdate
    void syncDefaultKey() {
        this.defaultKey = (isDefault && active) ? outletType + ":" + outletId : null;
    }
}
