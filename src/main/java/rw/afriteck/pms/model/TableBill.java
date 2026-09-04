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
    private UUID id;
    private String billNo;

    @ManyToOne
    @JoinColumn(name = "table_master_id")
    private TableMaster tableMaster;
}
