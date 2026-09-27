package rw.afriteck.pms.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;

import java.util.UUID;

@Entity
@Table(name = "pos_inventory",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "hotel_branch_id"}))
@Getter
@Setter
@NoArgsConstructor
public class POSInventory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Long version;

    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand = 0;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private POSProduct product;

    @Column(name = "hotel_branch_id", nullable = false)
    private UUID hotelBranchId;

}
