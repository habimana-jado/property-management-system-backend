package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.enums.ETableBillItemStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "table_bill_items")
public class TableBillItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private Integer transactionQuantity;
    private String remarks;
    private Boolean isDeleted = Boolean.FALSE;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceAtOrderTime;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    @Enumerated(EnumType.STRING)
    private ETableBillItemStatus status = ETableBillItemStatus.ACTIVE;

    private String voidReason;
    private Instant voidedAt;
    // TODO: @ManyToOne private Staff voidedBy;

    @ManyToOne
    @JoinColumn(name = "menu_master_id")
    private MenuMaster menuMaster;

    @ManyToOne
    @JoinColumn(name = "table_bill_id")
    private TableBill tableBill;
}
