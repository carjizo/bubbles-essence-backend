package com.bubblesessence.asistente;

import com.bubblesessence.ventas.producto.Producto;
import com.bubblesessence.ventas.producto.ProductoRepository;
import com.bubblesessence.ventas.productoingrediente.ProductoIngrediente;
import com.bubblesessence.ventas.productoingrediente.ProductoIngredienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Arma el catálogo completo como texto plano para mandarlo como contexto
 * al modelo en cada pregunta ("prompt stuffing"). Con pocos productos esto
 * es más simple y barato que un RAG con búsqueda vectorial; si el catálogo
 * crece mucho (decenas/cientos de productos), ahí sí conviene migrar a
 * embeddings + pgvector en Supabase para traer solo los productos
 * relevantes en vez del catálogo entero.
 */
@Service
@RequiredArgsConstructor
public class CatalogoContextService {

    private final ProductoRepository productoRepository;
    private final ProductoIngredienteRepository productoIngredienteRepository;

    /**
     * @Transactional es necesario acá: sin esto, cada llamada a un
     * repository (findByActivo, findByProducto_Id) abre y cierra su propia
     * sesión de Hibernate, y para cuando el stream de abajo intenta leer
     * pi.getIngrediente().getNombre() (relación LAZY), la sesión ya se
     * cerró -> LazyInitializationException ("no Session"). Con
     * @Transactional, todo el método comparte una sola sesión abierta.
     */
    @Transactional(readOnly = true)
    public String construirContexto() {
        List<Producto> productos = productoRepository.findByActivo(true);

        if (productos.isEmpty()) {
            return "El catálogo no tiene productos activos en este momento.";
        }

        StringBuilder sb = new StringBuilder();
        for (Producto producto : productos) {
            sb.append("- ").append(producto.getNombre());
            sb.append(" | Precio: S/ ").append(producto.getPrecio());

            if (producto.getStock() != null) {
                sb.append(producto.getStock() > 0 ? " | Disponible" : " | Agotado");
            }

            if (producto.getDescripcion() != null && !producto.getDescripcion().isBlank()) {
                sb.append(" | Descripción: ").append(producto.getDescripcion());
            }

            String ingredientes = obtenerIngredientesComoTexto(producto.getId());
            if (!ingredientes.isBlank()) {
                sb.append(" | Ingredientes: ").append(ingredientes);
            }

            sb.append("\n");
        }
        return sb.toString();
    }

    private String obtenerIngredientesComoTexto(Integer productoId) {
        List<ProductoIngrediente> receta = productoIngredienteRepository.findByProducto_Id(productoId);
        return receta.stream()
                .map(pi -> {
                    String nombre = pi.getIngrediente().getNombre();
                    String cantidad = pi.getCantidadReferencial();
                    return (cantidad != null && !cantidad.isBlank())
                            ? "%s (%s)".formatted(nombre, cantidad)
                            : nombre;
                })
                .collect(Collectors.joining(", "));
    }
}