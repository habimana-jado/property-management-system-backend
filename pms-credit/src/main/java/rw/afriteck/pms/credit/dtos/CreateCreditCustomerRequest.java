package rw.afriteck.pms.credit.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateCreditCustomerRequest(
        @NotBlank String fullName,
        @NotBlank String phoneNumber,
        @NotNull @DecimalMin("0.00") BigDecimal creditLimit
) {}

