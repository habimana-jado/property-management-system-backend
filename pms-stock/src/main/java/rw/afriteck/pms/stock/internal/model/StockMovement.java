package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Immutable;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.api.enums.EMovementType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Immutable
@Check(constraints = "quantity <> 0")
@Table(name = "stock_movement",
        indexes = {
                @Index(name = "idx_mov_store_item_time", columnList = "store_id, item_id, occurred_at"),
                @Index(name = "idx_mov_source", columnList = "source_type, source_id"),
                @Index(name = "idx_mov_document_line", columnList = "document_line_id"),
                @Index(name = "idx_mov_item_time", columnList = "item_id, occurred_at"),
                @Index(name = "idx_mov_time", columnList = "occurred_at")
        })
@Getter
@Setter
public class StockMovement extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private StockItem item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EMovementType movementType;

    // signed, base unit: positive = in, negative = out
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    // set for document-driven movements (receipt, transfer, adjustment, ...)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_line_id")
    private StockDocumentLine documentLine;

    // set for outlet sales (POS sale line / table bill line)
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 30)
    private EMovementSourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "source_line_id")
    private UUID sourceLineId;

    // unique => a movement can be reversed only once
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_of_id", unique = true)
    private StockMovement reversalOf;

    private String reason;

    private String note;

    // Staff ID From Context
    @Column(nullable = false)
    private UUID performedBy;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
