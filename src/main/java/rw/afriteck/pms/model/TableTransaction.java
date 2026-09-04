package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "table_transactions")
@Data
public class TableTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private int transactionQuantity;
    private String remarks;
    private Boolean isDeleted = Boolean.FALSE;

    @ManyToOne
    @JoinColumn(name = "menu_master_id")
    private MenuMaster menuMaster;

    @ManyToOne
    @JoinColumn(name = "table_bill_id")
    private TableBill tableBill;
}
