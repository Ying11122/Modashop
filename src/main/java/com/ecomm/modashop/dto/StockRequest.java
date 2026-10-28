package com.ecomm.modashop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
        @NotNull Long tallaId,
        @Min(0) int unidades) {}