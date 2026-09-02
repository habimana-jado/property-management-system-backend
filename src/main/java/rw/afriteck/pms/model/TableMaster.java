package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import rw.afriteck.pms.enums.ETableStatus;

import java.util.UUID;

@Entity
@Table(name = "table_masters")
@Data
public class TableMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tableMasterId;
    private String tableNumber;
    private int tableCapacity;
    @Enumerated(EnumType.STRING)
    private ETableStatus tableStatus;

    @ManyToOne
    private Restaurant restaurant;
}
