package com.bubblesessence.ventas.producto;

import com.bubblesessence.ventas.productoingrediente.ProductoIngrediente;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Cada método devuelve un Specification<Producto> que se combina con
 * .and() en ProductoServiceImpl. Cualquiera que devuelva null se ignora
 * automáticamente (Specification.where(null) no agrega condición) — así
 * cada filtro es verdaderamente opcional sin if/else por combinación.
 */
public class ProductoSpecifications {

    private ProductoSpecifications() {
    }

    public static Specification<Producto> activoEs(Boolean activo) {
        if (activo == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }

    public static Specification<Producto> codigoContiene(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        String patron = "%" + codigo.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("codigo")), patron);
    }

    public static Specification<Producto> nombreContiene(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        String patron = "%" + nombre.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombre")), patron);
    }

    public static Specification<Producto> precioDesde(BigDecimal minimo) {
        if (minimo == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<BigDecimal>get("precio"), minimo);
    }

    public static Specification<Producto> precioHasta(BigDecimal maximo) {
        if (maximo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<BigDecimal>get("precio"), maximo);
    }

    /** Solo si viene en true; false/null no filtra (no significa "solo sin stock"). */
    public static Specification<Producto> soloConStock(Boolean soloConStock) {
        if (soloConStock == null || !soloConStock) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThan(root.<Integer>get("stock"), 0);
    }

    public static Specification<Producto> creadoDesde(LocalDate desde) {
        if (desde == null) {
            return null;
        }
        LocalDateTime inicioDelDia = desde.atStartOfDay();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaCreacion"), inicioDelDia);
    }

    public static Specification<Producto> creadoHasta(LocalDate hasta) {
        if (hasta == null) {
            return null;
        }
        LocalDateTime finDelDia = hasta.atTime(LocalTime.MAX);
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaCreacion"), finDelDia);
    }

    /**
     * "mie" encuentra jabones que tengan un ingrediente cuyo nombre
     * contenga "mie" (ej. "Miel"). Usa EXISTS con subquery a
     * producto_ingrediente + ingrediente en vez de un JOIN directo, para
     * no duplicar filas de producto cuando coincide más de un ingrediente.
     */
    public static Specification<Producto> contieneIngrediente(String termino) {
        if (termino == null || termino.isBlank()) {
            return null;
        }
        String patron = "%" + termino.trim().toLowerCase() + "%";

        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            var piRoot = subquery.from(ProductoIngrediente.class);
            subquery.select(piRoot.get("id"));
            subquery.where(
                    cb.equal(piRoot.get("producto"), root),
                    cb.like(cb.lower(piRoot.get("ingrediente").get("nombre")), patron)
            );
            return cb.exists(subquery);
        };
    }
}
