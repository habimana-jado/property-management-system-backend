package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.internal.enums.EStoreType;

import java.util.UUID;

@Entity
@Table(name = "stock_store",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"branch_id", "code"}),
                @UniqueConstraint(columnNames = {"main_store_key"})
        })
@Getter
@Setter
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HotelBranch branch;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EStoreType storeType;

    private boolean active = true;

    // Set only for the MAIN store: unique constraint => one MAIN store per branch.
    @Column(name = "main_store_key")
    private UUID mainStoreKey;

    @PrePersist
    @PreUpdate
    void syncMainStoreKey() {
        this.mainStoreKey = (storeType == EStoreType.MAIN) ? branch.getId() : null;
    }
}
