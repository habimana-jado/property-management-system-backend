package rw.afriteck.pms.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.pos.enums.ERegisterSessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pos_register_session")
@Getter
@Setter
@NoArgsConstructor
public class POSRegisterSession extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "opening_cash", nullable = false, precision = 10, scale = 2)
    private BigDecimal openingCash;

    @Column(name = "expected_closing_cash", precision = 10, scale = 2)
    private BigDecimal expectedClosingCash; // computed at close time: opening + cash sales - cash refunds

    @Column(name = "actual_closing_cash", precision = 10, scale = 2)
    private BigDecimal actualClosingCash; // counted by cashier at close

    @Column(precision = 10, scale = 2)
    private BigDecimal variance; // actual - expected, flagged if non-zero

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ERegisterSessionStatus status = ERegisterSessionStatus.OPEN;

    @Column(name = "opened_at", nullable = false, updatable = false)
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "hotel_branch_id", nullable = false)
    private UUID hotelBranchId;

    @Column(name = "cashier_id", nullable = false)
    private UUID cashierId; // FK to Staff (pms-auth)

}
