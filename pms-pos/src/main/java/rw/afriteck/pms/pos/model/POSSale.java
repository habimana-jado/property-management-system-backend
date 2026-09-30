package rw.afriteck.pms.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.pos.enums.ESaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_sale",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hotel_branch_id", "sale_number"}))
@Getter @Setter @NoArgsConstructor
public class POSSale extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sale_number", nullable = false, length = 30)
    private String saleNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ESaleStatus status = ESaleStatus.DRAFT;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "tax_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "customer_id")
    private UUID customerId; // nullable — walk-in default

    @Column(name = "cashier_id", nullable = false)
    private UUID cashierId;

    @Column(name = "hotel_branch_id", nullable = false)
    private UUID hotelBranchId;

    @ManyToOne
    @JoinColumn(name = "register_session_id", nullable = false)
    private POSRegisterSession registerSession;



}
