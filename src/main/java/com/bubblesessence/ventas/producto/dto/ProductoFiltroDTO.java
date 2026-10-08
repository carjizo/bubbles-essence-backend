package com.bubblesessence.ventas.producto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Filtros opcionales del listado de productos. Todos pueden ser null
 * (= "no filtrar por esto"). Agrupados en un record para no arrastrar una
 * firma de 6 parámetros sueltos por controller -> service.
 *
 * @param ingrediente  búsqueda predictiva: productos que tengan un
 *                     ingrediente cuyo nombre contenga este texto
 *                     (se ignora si trae menos de 3 caracteres).
 * @param precioMin    precio mínimo (inclusive).
 * @param precioMax    precio máximo (inclusive).
 * @param soloConStock true = solo productos con stock > 0 (false/null = no filtra).
 */
public record ProductoFiltroDTO(
        Boolean activo,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String ingrediente,
        String codigo,
        String nombre,
        BigDecimal precioMin,
        BigDecimal precioMax,
        Boolean soloConStock
) {
}
