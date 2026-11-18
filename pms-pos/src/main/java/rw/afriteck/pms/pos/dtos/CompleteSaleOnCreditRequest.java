package rw.afriteck.pms.pos.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CompleteSaleOnCreditRequest(
        @NotNull UUID customerId,
        @NotNull @FutureOrPresent LocalDate dueDate,
        @Valid List<TenderRequest> tenders   // optional up-front payment; null/empty = full credit
) {}

