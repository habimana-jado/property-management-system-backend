package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.internal.model.DocumentSequence;
import rw.afriteck.pms.stock.internal.repository.DocumentSequenceRepository;

@Component
@RequiredArgsConstructor
class DocumentNumberService {

    private final DocumentSequenceRepository sequences;

    /** Rows are seeded (one per DocumentType) when a branch's MAIN store is created. */
    String next(HotelBranch branch, EDocumentType type) {
        DocumentSequence seq = sequences.findForUpdate(branch.getId(), type).orElseThrow(
                () -> new StockRuleViolationException("Document sequence not initialised for " + type));
        return "%s-%s-%06d".formatted(branch.getId(), prefix(type), seq.next());
    }

    private static String prefix(EDocumentType t) {
        return switch (t) {
            case RECEIPT -> "RCV"; case TRANSFER -> "TRF"; case ADJUSTMENT -> "ADJ";
            case WRITE_OFF -> "WOF"; case SUPPLIER_RETURN -> "SRT"; case CUSTOMER_RETURN -> "CRT";
            case CONSUMPTION -> "CSM"; case OPENING_BALANCE -> "OPN";
        };
    }
}
