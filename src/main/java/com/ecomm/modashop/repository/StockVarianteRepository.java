package com.ecomm.modashop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecomm.modashop.model.StockVariante;

import jakarta.persistence.LockModeType;

public interface StockVarianteRepository extends JpaRepository<StockVariante, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockVariante s where s.producto.id = :productoId and s.talla.id = :tallaId")
    Optional<StockVariante> findForUpdate(@Param("productoId") Long productoId,
                                          @Param("tallaId") Long tallaId);
}