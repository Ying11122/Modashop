package com.ecomm.modashop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PedidoRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 30) String telefono,
        @Size(max = 500) String notas) {}