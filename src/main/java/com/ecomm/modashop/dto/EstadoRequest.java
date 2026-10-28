package com.ecomm.modashop.dto;

import com.ecomm.modashop.model.EstadoPedido;

import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull EstadoPedido estado) {}