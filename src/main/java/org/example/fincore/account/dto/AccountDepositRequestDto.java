package org.example.fincore.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountDepositRequestDto(
        @NotNull
        @DecimalMin(value = "1")
        @Digits(integer = 19, fraction = 0)
        BigDecimal amount
        ) {
}
