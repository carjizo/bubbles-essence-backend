package com.bubblesessence.ventas.producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * JpaSpecificationExecutor permite combinar filtros opcionales (activo,
 * rango de fechas, ingrediente) de forma dinámica sin tener que escribir
 * un método derivado por cada combinación posible -> ver
 * ProductoSpecifications y ProductoServiceImpl.listar().
 */
public interface ProductoRepository extends JpaRepository<Producto, Integer>,
        JpaSpecificationExecutor<Producto> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByCodigoIgnoreCase(String codigo);

    List<Producto> findByActivo(Boolean activo);
}
