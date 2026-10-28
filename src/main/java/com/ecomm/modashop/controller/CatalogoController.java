package com.ecomm.modashop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecomm.modashop.model.Categoria;
import com.ecomm.modashop.model.Etiqueta;
import com.ecomm.modashop.model.Talla;
import com.ecomm.modashop.repository.CategoriaRepository;
import com.ecomm.modashop.repository.EtiquetaRepository;
import com.ecomm.modashop.repository.TallaRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogoController {

    private final CategoriaRepository categoriaRepo;
    private final TallaRepository tallaRepo;
    private final EtiquetaRepository etiquetaRepo;

    @GetMapping("/categorias")
    public List<Categoria> categorias() { return categoriaRepo.findAll(); }

    @GetMapping("/tallas")
    public List<Talla> tallas() { return tallaRepo.findAll(); }

    @GetMapping("/etiquetas")
    public List<Etiqueta> etiquetas() { return etiquetaRepo.findAll(); }
}