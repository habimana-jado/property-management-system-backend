package rw.afriteck.pms.stock.internal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.api.enums.EDocumentType;

import java.util.UUID;

@Entity
@Table(name = "stock_document_sequence",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "document_type"}))
@Getter
@Setter
public class DocumentSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HotelBranch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private EDocumentType documentType;

    @Column(nullable = false)
    private long lastNumber = 0;

    public long next() { return ++lastNumber; }
}
