package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_stocktake_line",
        uniqueConstraints = @UniqueConstraint(columnNames = {"stocktake_id", "item_id"}))
@Getter
@Setter
public class StocktakeLine extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Stocktake stocktake;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StockItem item;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal expectedQuantity;       // snapshot when counting starts

    @Column(precision = 19, scale = 4)
    private BigDecimal countedQuantity;        // null until counted

    @Transient
    public BigDecimal getVariance() {
        return countedQuantity == null ? null : countedQuantity.subtract(expectedQuantity);
    }
}
