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

    /**
     * Rows are seeded (one per DocumentType) when a branch's MAIN store is created.
     * Format: &lt;branch prefix&gt;-&lt;type&gt;-&lt;6-digit counter&gt;, e.g. 3F2A91C4-RCV-000001 (19 characters;
     * StockDocument.documentNo holds 40).
     */
    String next(HotelBranch branch, EDocumentType type) {
        DocumentSequence seq = sequences.findForUpdate(branch.getId(), type).orElseThrow(
                () -> new StockRuleViolationException("Document sequence not initialised for " + type));
        return "%s-%s-%06d".formatted(branchPrefix(branch), prefix(type), seq.next());
    }

    /**
     * Short, stable prefix for a branch. HotelBranch has no code yet, so this uses the last 8 hex digits of its id
     * (random for any UUID version, unlike the leading digits of a time-ordered UUID). When HotelBranch gets a
     * code, return it here: this is the only place that decides the prefix.
     */
    private static String branchPrefix(HotelBranch branch) {
        String id = branch.getId().toString().replace("-", "");
        return id.substring(id.length() - 8).toUpperCase();
    }

    private static String prefix(EDocumentType t) {
        return switch (t) {
            case RECEIPT -> "RCV"; case TRANSFER -> "TRF"; case ADJUSTMENT -> "ADJ";
            case WRITE_OFF -> "WOF"; case SUPPLIER_RETURN -> "SRT"; case CUSTOMER_RETURN -> "CRT";
            case CONSUMPTION -> "CSM"; case OPENING_BALANCE -> "OPN";
        };
    }
}