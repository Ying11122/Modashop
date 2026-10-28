package com.ecomm.modashop.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecomm.modashop.service.ImagenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/imagenes")
@RequiredArgsConstructor
public class AdminImagenController {

    private final ImagenService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> subir(@RequestParam("file") MultipartFile file) {
        return Map.of("url", service.guardar(file));
    }
}