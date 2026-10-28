package com.ecomm.modashop.dto;

public record PedidoLineaResponse(
        Long productoId,
        String producto,
        String color,
        String talla,
        int cantidad) {}