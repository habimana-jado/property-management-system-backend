package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.common.tenant.AuditableTenantOwned;

import java.util.UUID;

@Entity
@Table(name = "stock_category",
        uniqueConstraints = @UniqueConstraint(name = "uk_stock_category_hotel_code",
                columnNames = {"hotel_id", "code"}))
@Getter
@Setter
public class StockCategory extends AuditableTenantOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private StockCategory parent; // optional tree

    private boolean active = true;
}
