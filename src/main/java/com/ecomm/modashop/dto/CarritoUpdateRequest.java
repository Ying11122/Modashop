package com.ecomm.modashop.dto;

import jakarta.validation.constraints.Min;

public record CarritoUpdateRequest(@Min(1) int cantidad) {}