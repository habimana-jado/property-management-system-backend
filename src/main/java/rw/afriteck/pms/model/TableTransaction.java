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
    private UUID tableTransactionId;
    private int transactionQuantity;
    private String remarks;
    private Boolean isDeleted = Boolean.FALSE;

    @ManyToOne
    private MenuMaster menuMaster;

    @ManyToOne
    private TableBill tableBill;
}
