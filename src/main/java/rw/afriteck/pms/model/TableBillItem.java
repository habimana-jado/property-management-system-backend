package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
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

    @ManyToOne
    @JoinColumn(name = "menu_master_id")
    private MenuMaster menuMaster;

    @ManyToOne
    @JoinColumn(name = "table_bill_id")
    private TableBill tableBill;
}
