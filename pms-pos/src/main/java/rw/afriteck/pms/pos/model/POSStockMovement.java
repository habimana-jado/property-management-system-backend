package rw.afriteck.pms.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.pos.dtos.EAdjustmentReason;
import rw.afriteck.pms.pos.enums.EStockMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_stock_movement")
@Getter
@Setter
@NoArgsConstructor
public class POSStockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private EStockMovementType movementType;

    @Column(nullable = false)
    private Integer quantity; // positive = stock in, negative = stock out

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_reason", length = 30)
    private EAdjustmentReason adjustmentReason;

    @Column(name = "purchase_price_at_restock", precision = 10, scale = 2)
    private BigDecimal purchasePriceAtRestock;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    private POSSale sale; // FK — nullable, since RESTOCK/ADJUSTMENT movements aren't tied to a sale

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private POSProduct product;

    @Column(name = "hotel_branch_id", nullable = false)
    private UUID hotelBranchId;

}
