package com.ecomm.modashop.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ecomm.modashop.dto.CarritoAddRequest;
import com.ecomm.modashop.dto.CarritoItemResponse;
import com.ecomm.modashop.dto.CarritoUpdateRequest;
import com.ecomm.modashop.service.CarritoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private static final String CABECERA = "X-Cart-Id";

    private final CarritoService service;

    @GetMapping
    public List<CarritoItemResponse> listar(@RequestHeader(CABECERA) String carritoId) {
        return service.listar(carritoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarritoItemResponse anadir(@RequestHeader(CABECERA) String carritoId,
                                      @Valid @RequestBody CarritoAddRequest req) {
        return service.anadir(carritoId, req);
    }

    @PutMapping("/{id}")
    public CarritoItemResponse actualizar(@RequestHeader(CABECERA) String carritoId,
                                          @PathVariable Long id,
                                          @Valid @RequestBody CarritoUpdateRequest req) {
        return service.actualizar(carritoId, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(CABECERA) String carritoId, @PathVariable Long id) {
        service.eliminar(carritoId, id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vaciar(@RequestHeader(CABECERA) String carritoId) {
        service.vaciar(carritoId);
    }
}