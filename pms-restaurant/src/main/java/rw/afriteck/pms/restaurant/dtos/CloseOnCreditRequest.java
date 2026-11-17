package rw.afriteck.pms.restaurant.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CloseOnCreditRequest(
        @NotNull UUID customerId,
        @NotNull LocalDate dueDate
) {}
