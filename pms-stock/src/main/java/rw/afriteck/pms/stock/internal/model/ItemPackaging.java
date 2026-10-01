package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_item_packaging",
        uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "unit_id"}))
@Getter
@Setter
public class ItemPackaging {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StockItem item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UnitOfMeasure unit; // e.g. CARTON

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityInBaseUnit; // e.g. 24
}
