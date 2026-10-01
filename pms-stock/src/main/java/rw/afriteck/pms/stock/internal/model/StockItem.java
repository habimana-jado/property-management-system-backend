package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.stock.internal.enums.EItemType;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_item")
@Getter
@Setter
public class StockItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
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
