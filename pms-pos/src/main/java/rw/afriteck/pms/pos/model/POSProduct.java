package rw.afriteck.pms.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.common.enums.ERecordStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pos_product",
        uniqueConstraints =
                {
                        @UniqueConstraint(columnNames = {"hotel_branch_id", "sku"}),
                        @UniqueConstraint(columnNames = {"hotel_branch_id", "barcode"})
                })
@Getter
@Setter
@NoArgsConstructor
public class POSProduct extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String barcode;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "track_inventory", nullable = false)
    private boolean trackInventory = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ERecordStatus status = ERecordStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private POSProductCategory category;

    @Column(name = "hotel_branch_id", nullable = false)
    private UUID hotelBranchId;

}
