package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.common.tenant.AuditableTenantOwned;
import rw.afriteck.pms.stock.api.enums.EItemType;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_stock_item_hotel_code",
                columnNames = {"hotel_id", "code"}))
@Getter
@Setter
public class StockItem extends AuditableTenantOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StockCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UnitOfMeasure baseUnit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EItemType itemType;

    // in base unit; one global value per item
    @Column(precision = 19, scale = 4)
    private BigDecimal reorderLevel;

    private boolean active = true;
}
