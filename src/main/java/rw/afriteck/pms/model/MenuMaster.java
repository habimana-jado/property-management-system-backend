package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.afriteck.pms.enums.EMenuItemType;
import rw.afriteck.pms.enums.EPackageType;
import rw.afriteck.pms.enums.ERecordStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "menu_masters")
public class MenuMaster extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String menuItemName;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EMenuItemType menuItemType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EPackageType packageType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ERecordStatus status;

    @ManyToOne
    @JoinColumn(name = "menu_category_id")
    private MenuCategory menuCategory;
}
