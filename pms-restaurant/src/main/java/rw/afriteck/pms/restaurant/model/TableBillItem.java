package rw.afriteck.pms.restaurant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.restaurant.enums.ETableBillItemStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "table_bill_items")
public class TableBillItem extends Auditable {
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

    @ManyToOne
    @JoinColumn(name = "menu_master_id")
    private MenuMaster menuMaster;

    @ManyToOne
    @JoinColumn(name = "table_bill_id")
    private TableBill tableBill;
}
