package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_document_line")
@Getter
@Setter
public class StockDocumentLine extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StockDocument document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StockItem item;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;               // base unit

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal enteredQuantity;        // as typed by the user

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UnitOfMeasure enteredUnit;
}
