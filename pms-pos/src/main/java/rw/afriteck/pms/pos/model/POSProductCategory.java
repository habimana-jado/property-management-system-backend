package rw.afriteck.pms.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.common.enums.ERecordStatus;

import java.util.UUID;

@Entity
@Table(name = "pos_product_category",
        uniqueConstraints = @UniqueConstraint(columnNames = {"hotel_branch_id", "category_name"}))
@Getter
@Setter
@NoArgsConstructor
public class POSProductCategory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ERecordStatus status = ERecordStatus.ACTIVE;

    @Column(name = "hotel_branch_id", nullable = false)
    private UUID hotelBranchId;

}
