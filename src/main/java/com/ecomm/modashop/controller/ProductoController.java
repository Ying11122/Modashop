package com.ecomm.modashop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomm.modashop.dto.ProductoResponse;
import com.ecomm.modashop.service.ProductoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService service;

    @GetMapping
    public List<ProductoResponse> listar(
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) Long talla,
            @RequestParam(required = false) Long etiqueta,
            @RequestParam(required = false) String q) {
        return service.listar(categoria, talla, etiqueta, q);
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }
}