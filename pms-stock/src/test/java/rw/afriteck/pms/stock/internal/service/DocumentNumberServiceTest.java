package rw.afriteck.pms.stock.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.internal.model.DocumentSequence;
import rw.afriteck.pms.stock.internal.model.StockDocument;
import rw.afriteck.pms.stock.internal.repository.DocumentSequenceRepository;

@ExtendWith(MockitoExtension.class)
class DocumentNumberServiceTest {

    @Mock DocumentSequenceRepository sequences;

    @Test
    void everyDocumentNumberFitsTheColumn_andCountsUpPerType() throws Exception {
        // read the real column length, so this fails if either the format or the column changes
        int columnLength = StockDocument.class.getDeclaredField("documentNo").getAnnotation(Column.class).length();
        HotelBranch branch = branch(UUID.randomUUID());
        DocumentNumberService service = new DocumentNumberService(sequences);

        for (EDocumentType type : EDocumentType.values()) {
            when(sequences.findForUpdate(branch.getId(), type)).thenReturn(Optional.of(new DocumentSequence()));

            String first = service.next(branch, type);
            String second = service.next(branch, type);

            assertThat(first).as(type + " number must fit documentNo(%d)", columnLength)
                    .hasSizeLessThanOrEqualTo(columnLength);
            assertThat(first).endsWith("-000001");
            assertThat(second).endsWith("-000002");
        }
    }

    @Test
    void branchesGetTheirOwnPrefix() {
        DocumentNumberService service = new DocumentNumberService(sequences);
        HotelBranch a = branch(UUID.fromString("11111111-2222-4333-8444-aaaaaaaaaaaa"));
        HotelBranch b = branch(UUID.fromString("11111111-2222-4333-8444-bbbbbbbbbbbb"));
        when(sequences.findForUpdate(a.getId(), EDocumentType.RECEIPT)).thenReturn(Optional.of(new DocumentSequence()));
        when(sequences.findForUpdate(b.getId(), EDocumentType.RECEIPT)).thenReturn(Optional.of(new DocumentSequence()));

        assertThat(service.next(a, EDocumentType.RECEIPT)).isEqualTo("AAAAAAAA-RCV-000001");
        assertThat(service.next(b, EDocumentType.RECEIPT)).isEqualTo("BBBBBBBB-RCV-000001");
    }

    private static HotelBranch branch(UUID id) {
        HotelBranch b = new HotelBranch();
        ReflectionTestUtils.setField(b, "id", id);
        return b;
    }
}