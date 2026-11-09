package rw.afriteck.pms.stock.web.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;

import java.util.List;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID fromStoreId,
        @NotNull UUID toStoreId,
        @NotNull ETransferPurpose purpose,
        @Size(max = 500) String note,
        @NotNull @Size(min = 1, max = 500) List<@Valid DocumentLineRequest> lines)
{
    @JsonIgnore
    @AssertTrue(message = "A damage report needs a note explaining what happened")
    public boolean isNoteProvidedForDamageReport() {
        return purpose != ETransferPurpose.DAMAGE_REPORT || (note != null && !note.isBlank());
    }
}
