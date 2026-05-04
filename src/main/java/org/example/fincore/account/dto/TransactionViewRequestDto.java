package org.example.fincore.account.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TransactionViewRequestDto(
        @NotNull @Min(0) Integer page,
        @NotNull @Min(1) @Max(50) Integer size
){
}
