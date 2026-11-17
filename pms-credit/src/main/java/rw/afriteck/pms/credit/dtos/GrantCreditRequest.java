package rw.afriteck.pms.credit.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rw.afriteck.pms.common.enums.EPayableType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

// internal contract: called by other modules, not exposed over REST
public record GrantCreditRequest(
        @NotNull UUID customerId,
        @NotNull EPayableType sourceType,
        @NotNull UUID sourceId,
        @NotBlank String sourceReference,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull LocalDate dueDate
) {}
