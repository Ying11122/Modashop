package com.ecomm.modashop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomm.modashop.model.CarritoItem;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    List<CarritoItem> findByCarritoIdOrderById(String carritoId);

    Optional<CarritoItem> findByIdAndCarritoId(Long id, String carritoId);

    Optional<CarritoItem> findByCarritoIdAndProductoIdAndTallaId(
            String carritoId, Long productoId, Long tallaId);

    void deleteByCarritoId(String carritoId);

    // Lo usa ProductoService.eliminar: quita el producto de todos los carritos
    void deleteByProductoId(Long productoId);
}