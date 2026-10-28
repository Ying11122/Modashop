package com.ecomm.modashop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomm.modashop.model.EstadoPedido;
import com.ecomm.modashop.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByIdDesc();

    List<Pedido> findByEstadoOrderByIdDesc(EstadoPedido estado);
}