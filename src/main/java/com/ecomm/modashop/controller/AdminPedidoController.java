package com.ecomm.modashop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomm.modashop.dto.EstadoRequest;
import com.ecomm.modashop.dto.PedidoResponse;
import com.ecomm.modashop.model.EstadoPedido;
import com.ecomm.modashop.service.PedidoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/pedidos")
@RequiredArgsConstructor
public class AdminPedidoController {

    private final PedidoService service;

    @GetMapping
    public List<PedidoResponse> listar(@RequestParam(required = false) EstadoPedido estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}/estado")
    public PedidoResponse cambiarEstado(@PathVariable Long id,
                                        @Valid @RequestBody EstadoRequest req) {
        return service.cambiarEstado(id, req.estado());
    }
}