package rw.afriteck.pms.stock.web.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ReasonedDocumentRequest(
        @NotNull UUID storeId,
        @NotBlank @Size(max = 500) String reason,
        @Size(max = 100) String externalReference,
        @NotNull @Size(min = 1, max = 500) List<@Valid DocumentLineRequest> lines) {
}
