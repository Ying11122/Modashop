package com.ecomm.modashop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class PedidoLinea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private Long tallaId;

    @Column(nullable = false, length = 120)
    private String nombreProducto;

    @Column(length = 40)
    private String color;

    @Column(nullable = false, length = 10)
    private String talla;

    @Column(nullable = false)
    private int cantidad;
}