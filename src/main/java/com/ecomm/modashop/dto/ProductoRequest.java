package com.ecomm.modashop.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductoRequest(
        @NotBlank String nombre,
        String descripcion,
        String color,
        String imagenUrl,
        @NotNull Long categoriaId,
        List<Long> etiquetaIds,
        List<@Valid StockRequest> stock) {}