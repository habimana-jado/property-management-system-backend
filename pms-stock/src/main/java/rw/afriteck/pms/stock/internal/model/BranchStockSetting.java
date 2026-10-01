package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.property.model.HotelBranch;

import java.util.UUID;

@Entity
@Table(name = "stock_branch_settings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id"}))
@Getter
@Setter
public class BranchStockSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HotelBranch branch;

    private boolean allowNegativeStock = false;
}
