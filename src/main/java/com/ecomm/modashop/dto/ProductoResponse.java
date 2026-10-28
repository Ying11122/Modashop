package com.ecomm.modashop.dto;

import java.util.List;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        String color,
        String imagenUrl,
        String categoria,
        List<String> etiquetas,
        List<StockDTO> stock) {}