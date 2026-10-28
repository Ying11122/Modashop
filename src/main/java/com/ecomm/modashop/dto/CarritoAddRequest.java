package com.ecomm.modashop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CarritoAddRequest(
        @NotNull Long productoId,
        @NotNull Long tallaId,
        @Min(1) int cantidad) {}