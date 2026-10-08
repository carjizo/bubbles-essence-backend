package com.bubblesessence.ventas.producto.dto;

import java.time.LocalDate;

/**
 * Filtros opcionales del listado de productos. Todos pueden ser null
 * (= "no filtrar por esto"). Agrupados en un record para no arrastrar una
 * firma de 6 parámetros sueltos por controller -> service.
 *
 * @param ingrediente búsqueda predictiva: productos que tengan un
 *                    ingrediente cuyo nombre contenga este texto
 *                    (se ignora si trae menos de 3 caracteres).
 */
public record ProductoFiltroDTO(
        Boolean activo,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String ingrediente,
        String codigo,
        String nombre
) {
}
