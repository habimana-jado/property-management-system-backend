package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.enums.EBillStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "table_bills")
public class TableBill {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String billNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EBillStatus billStatus;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "table_master_id")
    private TableMaster tableMaster;
}
