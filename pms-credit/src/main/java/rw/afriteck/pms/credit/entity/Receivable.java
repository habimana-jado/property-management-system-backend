package rw.afriteck.pms.credit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.tenant.AuditableTenantOwned;
import rw.afriteck.pms.credit.enums.EReceivableStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "receivables",
        uniqueConstraints = @UniqueConstraint(name = "uk_receivable_source", columnNames = {"source_type", "source_id"}),
        indexes = @Index(name = "idx_receivable_hotel_customer_status", columnList = "hotel_id, customer_id, status"))
@Getter
@Setter
@NoArgsConstructor
public class Receivable extends AuditableTenantOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CreditCustomer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private EPayableType sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "source_reference", nullable = false)
    private String sourceReference; // e.g. billNo, for display

    @Column(name = "original_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "amount_paid", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "origin_branch_id")
    private UUID originBranchId;   // where the sale happened; informational

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EReceivableStatus status = EReceivableStatus.OUTSTANDING;

    @Column(name = "written_off_amount", precision = 19, scale = 2)
    private BigDecimal writtenOffAmount;

    @Column(name = "write_off_reason")
    private String writeOffReason;

    @Column(name = "written_off_by")
    private String writtenOffBy;

    @Column(name = "written_off_at")
    private Instant writtenOffAt;

    public BigDecimal outstandingBalance() {
        if (status == EReceivableStatus.WRITTEN_OFF) {
            return BigDecimal.ZERO; // no longer owed; the written-off amount lives in writtenOffAmount
        }
        return originalAmount.subtract(amountPaid);
    }

    public boolean isOverdue() {
        return isActive() && dueDate.isBefore(LocalDate.now());
    }

    public boolean isActive() {
        return status == EReceivableStatus.OUTSTANDING || status == EReceivableStatus.PARTIALLY_SETTLED;
    }
}
