package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_balance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "item_id"}),
        indexes = @Index(name = "idx_balance_item", columnList = "item_id"))
@Getter
@Setter
public class StockBalance extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StockItem item;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    public void apply(BigDecimal delta) {
        this.quantityOnHand = this.quantityOnHand.add(delta);
    }
}
