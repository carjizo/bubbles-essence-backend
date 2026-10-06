package com.bubblesessence.ventas.productoingrediente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoIngredienteRepository extends JpaRepository<ProductoIngrediente, Long> {

    List<ProductoIngrediente> findByProducto_Id(Integer productoId);

    boolean existsByProducto_IdAndIngrediente_Id(Integer productoId, Integer ingredienteId);

    /**
     * Trae la receta de VARIOS productos en una sola consulta (en vez de
     * llamar findByProducto_Id en un loop, que sería N+1). El JOIN FETCH
     * además trae el Ingrediente relacionado en la misma consulta, así no
     * se dispara una consulta extra por cada acceso a
     * productoIngrediente.getIngrediente() (ese campo es LAZY).
     */
    @Query("""
            SELECT pi FROM ProductoIngrediente pi
            JOIN FETCH pi.ingrediente
            WHERE pi.producto.id IN :productoIds
            """)
    List<ProductoIngrediente> findByProductoIdInConIngrediente(@Param("productoIds") List<Integer> productoIds);
}