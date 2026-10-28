package com.ecomm.modashop.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        LocalDateTime fecha,
        String estado,
        String nombre,
        String email,
        String telefono,
        String notas,
        List<PedidoLineaResponse> lineas) {}