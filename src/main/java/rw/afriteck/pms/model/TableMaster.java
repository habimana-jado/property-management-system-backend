package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.enums.ETableStatus;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "table_masters",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_table_master_restaurant_name",
                columnNames = {"restaurant_id", "table_number"}
        ))
public class TableMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String tableNumber;
    private int tableCapacity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ETableStatus tableStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ERecordStatus recordStatus;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
}
