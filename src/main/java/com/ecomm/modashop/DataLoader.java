package com.ecomm.modashop;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ecomm.modashop.model.Categoria;
import com.ecomm.modashop.model.Etiqueta;
import com.ecomm.modashop.model.Talla;

import jakarta.persistence.EntityManager;

@Component
public class DataLoader implements CommandLineRunner {

    private final EntityManager em;

    public DataLoader(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Long categorias = em.createQuery("select count(c) from Categoria c", Long.class)
                            .getSingleResult();
        if (categorias > 0) return; // ya hay datos, no repetir

        List.of("Camisetas", "Pantalones", "Vestidos")
            .forEach(n -> em.persist(new Categoria(n)));

        List.of("XS", "S", "M", "L", "XL")
            .forEach(n -> em.persist(new Talla(n)));

        List.of("Nueva temporada", "Oferta", "Algodón", "Verano", "Básico")
            .forEach(n -> em.persist(new Etiqueta(n)));

            System.out.println(">>> DataLoader ejecutado");
    }
}