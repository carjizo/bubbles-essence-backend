package com.bubblesessence.asistente;

import com.bubblesessence.ventas.producto.Producto;
import com.bubblesessence.ventas.producto.ProductoRepository;
import com.bubblesessence.ventas.productoingrediente.ProductoIngrediente;
import com.bubblesessence.ventas.productoingrediente.ProductoIngredienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Arma el catálogo completo como texto plano para mandarlo como contexto
 * al modelo en cada pregunta ("prompt stuffing"). Con pocos productos esto
 * es más simple y barato que un RAG con búsqueda vectorial; si el catálogo
 * crece mucho (decenas/cientos de productos), ahí sí conviene migrar a
 * embeddings + pgvector en Supabase para traer solo los productos
 * relevantes en vez del catálogo entero.
 *
 * OPTIMIZACIONES:
 * 1. Una sola consulta para la receta de TODOS los productos (JOIN FETCH
 *    del ingrediente incluido), en vez de una consulta por producto
 *    (N+1) — ver ProductoIngredienteRepository.findByProductoIdInConIngrediente.
 * 2. Caché en memoria por TTL_CACHE: el catálogo no cambia cada segundo,
 *    así que no hace falta volver a consultar Supabase en cada mensaje
 *    del chat. Un producto editado tarda como máximo este tiempo en
 *    reflejarse en las respuestas del asistente.
 */
@Service
@RequiredArgsConstructor
public class CatalogoContextService {

    private static final Duration TTL_CACHE = Duration.ofSeconds(60);

    private final ProductoRepository productoRepository;
    private final ProductoIngredienteRepository productoIngredienteRepository;

    private volatile String contextoCacheado;
    private volatile Instant cacheadoEn = Instant.MIN;

    /**
     * @Transactional es necesario para el acceso a pi.getIngrediente()
     * dentro del stream (relación LAZY) — aunque ahora con JOIN FETCH ya
     * viene cargado en la misma consulta, se deja igual por si en el
     * futuro se agrega algún otro acceso lazy acá.
     */
    @Transactional(readOnly = true)
    public String construirContexto() {
        if (contextoCacheado != null && Duration.between(cacheadoEn, Instant.now()).compareTo(TTL_CACHE) < 0) {
            return contextoCacheado;
        }

        List<Producto> productos = productoRepository.findByActivo(true);
        String contexto = productos.isEmpty()
                ? "El catálogo no tiene productos activos en este momento."
                : construirTextoCatalogo(productos);

        contextoCacheado = contexto;
        cacheadoEn = Instant.now();
        return contexto;
    }

    private String construirTextoCatalogo(List<Producto> productos) {
        List<Integer> productoIds = productos.stream().map(Producto::getId).toList();

        // 1 sola consulta para la receta de TODOS los productos a la vez.
        Map<Integer, List<ProductoIngrediente>> recetaPorProducto = productoIngredienteRepository
                .findByProductoIdInConIngrediente(productoIds)
                .stream()
                .collect(Collectors.groupingBy(pi -> pi.getProducto().getId()));

        StringBuilder sb = new StringBuilder();
        for (Producto producto : productos) {
            sb.append("- ").append(producto.getNombre());
            sb.append(" | Precio: S/ ").append(producto.getPrecio());

            if (producto.getStock() != null) {
                sb.append(producto.getStock() > 0
                        ? " | Stock disponible: %d unidades".formatted(producto.getStock())
                        : " | Agotado (0 unidades)");
            }

            if (producto.getDescripcion() != null && !producto.getDescripcion().isBlank()) {
                sb.append(" | Descripción: ").append(producto.getDescripcion());
            }

            List<ProductoIngrediente> receta = recetaPorProducto.getOrDefault(producto.getId(), List.of());
            String ingredientes = receta.stream()
                    .map(pi -> {
                        String nombre = pi.getIngrediente().getNombre();
                        String cantidad = pi.getCantidadReferencial();
                        return (cantidad != null && !cantidad.isBlank())
                                ? "%s (%s)".formatted(nombre, cantidad)
                                : nombre;
                    })
                    .collect(Collectors.joining(", "));
            if (!ingredientes.isBlank()) {
                sb.append(" | Ingredientes: ").append(ingredientes);
            }

            sb.append("\n");
        }
        return sb.toString();
    }
}