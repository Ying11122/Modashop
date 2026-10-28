package com.ecomm.modashop.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecomm.modashop.dto.CarritoAddRequest;
import com.ecomm.modashop.dto.CarritoItemResponse;
import com.ecomm.modashop.dto.CarritoUpdateRequest;
import com.ecomm.modashop.model.CarritoItem;
import com.ecomm.modashop.model.Producto;
import com.ecomm.modashop.model.StockVariante;
import com.ecomm.modashop.model.Talla;
import com.ecomm.modashop.repository.CarritoItemRepository;
import com.ecomm.modashop.repository.ProductoRepository;
import com.ecomm.modashop.repository.TallaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private static final Pattern UUID_VALIDO = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private final CarritoItemRepository carritoRepo;
    private final ProductoRepository productoRepo;
    private final TallaRepository tallaRepo;

    // ---------- GET ----------

    @Transactional(readOnly = true)
    public List<CarritoItemResponse> listar(String carritoId) {
        validar(carritoId);
        return carritoRepo.findByCarritoIdOrderById(carritoId)
                .stream().map(this::toResponse).toList();
    }

    // ---------- POST ----------

    @Transactional
    public CarritoItemResponse anadir(String carritoId, CarritoAddRequest req) {
        validar(carritoId);

        Producto producto = productoRepo.findById(req.productoId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        Talla talla = tallaRepo.findById(req.tallaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Talla no válida"));

        int disponible = disponible(producto, talla.getId());
        if (disponible == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Producto no disponible en la talla " + talla.getNombre());
        }

        // Si ya hay una línea con ese producto y talla en ESTE carrito, se suma
        CarritoItem item = carritoRepo
                .findByCarritoIdAndProductoIdAndTallaId(carritoId, producto.getId(), talla.getId())
                .orElseGet(() -> {
                    CarritoItem nuevo = new CarritoItem();
                    nuevo.setCarritoId(carritoId);
                    nuevo.setProducto(producto);
                    nuevo.setTalla(talla);
                    nuevo.setCantidad(0);
                    return nuevo;
                });

        int total = item.getCantidad() + req.cantidad();
        comprobarStock(total, disponible);
        item.setCantidad(total);

        return toResponse(carritoRepo.save(item));
    }

    // ---------- PUT ----------

    @Transactional
    public CarritoItemResponse actualizar(String carritoId, Long id, CarritoUpdateRequest req) {
        validar(carritoId);
        CarritoItem item = buscar(carritoId, id);
        comprobarStock(req.cantidad(),
                disponible(item.getProducto(), item.getTalla().getId()));
        item.setCantidad(req.cantidad());
        return toResponse(item); // se guarda al terminar la transacción
    }

    // ---------- DELETE ----------

    @Transactional
    public void eliminar(String carritoId, Long id) {
        validar(carritoId);
        carritoRepo.delete(buscar(carritoId, id));
    }

    @Transactional
    public void vaciar(String carritoId) {
        validar(carritoId);
        carritoRepo.deleteByCarritoId(carritoId);
    }

    // ---------- auxiliares ----------

    private void validar(String carritoId) {
        if (carritoId == null || !UUID_VALIDO.matcher(carritoId).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Identificador de carrito no válido");
        }
    }

    private CarritoItem buscar(String carritoId, Long id) {
        return carritoRepo.findByIdAndCarritoId(id, carritoId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Línea de carrito no encontrada"));
    }

    private int disponible(Producto producto, Long tallaId) {
        return producto.getStock().stream()
                .filter(s -> s.getTalla().getId().equals(tallaId))
                .mapToInt(StockVariante::getUnidades)
                .sum();
    }

    private void comprobarStock(int cantidad, int disponible) {
        if (cantidad > disponible) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo quedan " + disponible + " unidades en esa talla");
        }
    }

    private CarritoItemResponse toResponse(CarritoItem i) {
        Producto p = i.getProducto();
        return new CarritoItemResponse(
                i.getId(), p.getId(), p.getNombre(), p.getColor(), p.getImagenUrl(),
                p.getCategoria().getNombre(),
                i.getTalla().getId(), i.getTalla().getNombre(),
                i.getCantidad(), disponible(p, i.getTalla().getId()));
    }
}