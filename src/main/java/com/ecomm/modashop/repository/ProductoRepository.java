package com.ecomm.modashop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecomm.modashop.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("""
            select p from Producto p
            where (:categoriaId is null or p.categoria.id = :categoriaId)
              and (:q is null or lower(p.nombre) like :q)
              and (:tallaId is null or exists (
                    select 1 from p.stock s
                    where s.talla.id = :tallaId and s.unidades > 0))
              and (:etiquetaId is null or exists (
                    select 1 from p.etiquetas e
                    where e.id = :etiquetaId))
            order by p.nombre
            """)
    List<Producto> filtrar(@Param("categoriaId") Long categoriaId,
                           @Param("tallaId") Long tallaId,
                           @Param("etiquetaId") Long etiquetaId,
                           @Param("q") String q);
}