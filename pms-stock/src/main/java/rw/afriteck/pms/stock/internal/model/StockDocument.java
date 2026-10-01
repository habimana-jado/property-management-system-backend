package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.common.audit.Auditable;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;
import rw.afriteck.pms.stock.internal.enums.EDocumentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stock_document")
@Getter
@Setter
public class StockDocument extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String documentNo;                  // human readable, e.g. KGL-TRF-000123

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EDocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Store store;                        // primary / source store

    @ManyToOne(fetch = FetchType.LAZY)
    private Store counterpartStore;             // destination for transfers

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ETransferPurpose purpose;           // transfers only

    private String externalReference;

    private String supplierName;               // manual receipts

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EDocumentStatus status = EDocumentStatus.POSTED;

    @Column(nullable = false)
    private UUID postedBy;

    @Column(nullable = false)
    private Instant postedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockDocumentLine> lines = new ArrayList<>();

    public void addLine(StockDocumentLine line) {
        line.setDocument(this);
        lines.add(line);
    }
}
