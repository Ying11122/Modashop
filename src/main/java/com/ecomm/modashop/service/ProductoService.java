package com.ecomm.modashop.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecomm.modashop.dto.ProductoRequest;
import com.ecomm.modashop.dto.ProductoResponse;
import com.ecomm.modashop.dto.StockDTO;
import com.ecomm.modashop.dto.StockRequest;
import com.ecomm.modashop.model.Etiqueta;
import com.ecomm.modashop.model.Producto;
import com.ecomm.modashop.model.StockVariante;
import com.ecomm.modashop.model.Talla;
import com.ecomm.modashop.repository.CategoriaRepository;
import com.ecomm.modashop.repository.EtiquetaRepository;
import com.ecomm.modashop.repository.ProductoRepository;
import com.ecomm.modashop.repository.TallaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final CategoriaRepository categoriaRepo;
    private final EtiquetaRepository etiquetaRepo;
    private final TallaRepository tallaRepo;

    // ---------- GET ----------

        @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long categoriaId, Long tallaId, Long etiquetaId, String q) {
        // Texto vacío = sin filtro; si hay texto, lo preparamos para el LIKE
        String patron = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";

        return productoRepo.filtrar(categoriaId, tallaId, etiquetaId, patron)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    // ---------- POST ----------

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        Producto p = new Producto();
        aplicar(p, req);
        return toResponse(productoRepo.save(p));
    }

    // ---------- PUT ----------

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto p = buscar(id);
        aplicar(p, req);
        return toResponse(p); // la entidad está gestionada: se guarda al terminar la transacción
    }

    // ---------- DELETE ----------

    @Transactional
    public void eliminar(Long id) {
        Producto p = buscar(id);
        productoRepo.delete(p); // borra también su stock (cascade) y sus filas en producto_etiqueta
    }

    // ---------- auxiliares ----------

    private Producto buscar(Long id) {
        return productoRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    private void aplicar(Producto p, ProductoRequest req) {
        p.setNombre(req.nombre());
        p.setDescripcion(req.descripcion());
        p.setColor(req.color());
        p.setImagenUrl(req.imagenUrl());

        p.setCategoria(categoriaRepo.findById(req.categoriaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría no válida")));

        p.getEtiquetas().clear();
        if (req.etiquetaIds() != null) {
            p.getEtiquetas().addAll(etiquetaRepo.findAllById(req.etiquetaIds()));
        }

        sincronizarStock(p, req.stock());
    }

    private void sincronizarStock(Producto p, List<StockRequest> pedido) {
        List<StockRequest> nuevo = (pedido == null) ? List.of() : pedido;

        // 1. No se admiten tallas repetidas en la misma petición
        Set<Long> tallasPedidas = new HashSet<>();
        for (StockRequest s : nuevo) {
            if (!tallasPedidas.add(s.tallaId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Talla repetida en el stock");
            }
        }

        // 2. Eliminar las tallas que ya no vienen en la petición
        p.getStock().removeIf(sv -> !tallasPedidas.contains(sv.getTalla().getId()));

        // 3. Actualizar las existentes o crear las nuevas
        Map<Long, StockVariante> actuales = p.getStock().stream()
                .collect(Collectors.toMap(sv -> sv.getTalla().getId(), sv -> sv));

        for (StockRequest s : nuevo) {
            StockVariante sv = actuales.get(s.tallaId());
            if (sv != null) {
                sv.setUnidades(s.unidades());
            } else {
                Talla talla = tallaRepo.findById(s.tallaId()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Talla no válida"));
                StockVariante nueva = new StockVariante();
                nueva.setProducto(p);
                nueva.setTalla(talla);
                nueva.setUnidades(s.unidades());
                p.getStock().add(nueva);
            }
        }
    }

    private ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getDescripcion(), p.getColor(), p.getImagenUrl(),
                p.getCategoria().getNombre(),
                p.getEtiquetas().stream().map(Etiqueta::getNombre).sorted().toList(),
                p.getStock().stream()
                        .map(s -> new StockDTO(s.getTalla().getId(), s.getTalla().getNombre(), s.getUnidades()))
                        .toList());
    }
}