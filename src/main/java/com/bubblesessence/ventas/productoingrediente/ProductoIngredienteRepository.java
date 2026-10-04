package com.bubblesessence.ventas.productoingrediente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoIngredienteRepository extends JpaRepository<ProductoIngrediente, Long> {

    List<ProductoIngrediente> findByProducto_Id(Integer productoId);

    boolean existsByProducto_IdAndIngrediente_Id(Integer productoId, Integer ingredienteId);
}
