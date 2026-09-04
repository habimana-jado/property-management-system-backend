package rw.afriteck.pms.model;

import jakarta.persistence.*;
import lombok.Data;
import rw.afriteck.pms.enums.EMenuItemType;
import rw.afriteck.pms.enums.EPackageType;
import rw.afriteck.pms.enums.ERecordStatus;

import java.util.UUID;

@Entity
@Table(name = "menu_masters")
@Data
public class MenuMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String menuItemName;
    private Double unitPrice;
    @Enumerated(EnumType.STRING)
    private EMenuItemType menuItemType;
    @Enumerated(EnumType.STRING)
    private EPackageType packageType;
    @Enumerated(EnumType.STRING)
    private ERecordStatus status;

    @ManyToOne
    @JoinColumn(name = "menu_category_id")
    private MenuCategory menuCategory;
}
