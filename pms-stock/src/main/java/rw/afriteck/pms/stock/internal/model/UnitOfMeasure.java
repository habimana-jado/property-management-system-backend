package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.stock.internal.enums.EUnitGroup;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_unit_of_measure")
@Getter
@Setter
public class UnitOfMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String code; // KG, G, L, ML, PCS, CARTON

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EUnitGroup unitGroup;

    // G = 1, KG = 1000, ML = 1, L = 1000, PCS = 1
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal factorToGroupBase = BigDecimal.ONE;

}
