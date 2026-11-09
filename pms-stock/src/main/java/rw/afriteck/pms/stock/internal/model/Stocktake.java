package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.stock.api.enums.EStocktakeStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stock_stocktake",
        uniqueConstraints = @UniqueConstraint(columnNames = {"open_store_key"}),
        indexes = {@Index(name = "idx_stocktake_store_status", columnList = "store_id, status")})
@Getter
@Setter
public class Stocktake extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EStocktakeStatus status = EStocktakeStatus.DRAFT;

    private UUID startedBy;
    private Instant startedAt;
    private UUID approvedBy;
    private Instant approvedAt;
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    private StockDocument adjustmentDocument;  // set on approval

    // Store id while the stocktake is open, null once approved/cancelled:
    // unique constraint => at most one open stocktake per store.
    @Column(name = "open_store_key")
    private UUID openStoreKey;

    @OneToMany(mappedBy = "stocktake", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StocktakeLine> lines = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void syncOpenStoreKey() {
        boolean open = status == EStocktakeStatus.DRAFT
                || status == EStocktakeStatus.COUNTING
                || status == EStocktakeStatus.SUBMITTED;
        this.openStoreKey = open ? store.getId() : null;
    }

    public void addLine(StocktakeLine line) {
        line.setStocktake(this);
        lines.add(line);
    }
}
