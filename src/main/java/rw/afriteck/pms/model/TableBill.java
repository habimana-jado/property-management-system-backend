package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "table_bills")
@Data
public class TableBill {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tableBillId;
    private String billNo;

    @ManyToOne
    private TableMaster tableMaster;
}
