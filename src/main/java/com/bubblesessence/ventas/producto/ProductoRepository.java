package com.bubblesessence.ventas.producto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByCodigoIgnoreCase(String codigo);

    List<Producto> findByActivo(Boolean activo);
}
