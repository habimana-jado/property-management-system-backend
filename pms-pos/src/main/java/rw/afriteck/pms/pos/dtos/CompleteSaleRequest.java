package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CompleteSaleRequest(
        @NotEmpty List<TenderRequest> tenders
) {
}
