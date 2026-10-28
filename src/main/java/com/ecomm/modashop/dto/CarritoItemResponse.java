package com.ecomm.modashop.dto;

public record CarritoItemResponse(
        Long id,
        Long productoId,
        String nombre,
        String color,
        String imagenUrl,
        String categoria,
        Long tallaId,
        String talla,
        int cantidad,
        int disponible) {}