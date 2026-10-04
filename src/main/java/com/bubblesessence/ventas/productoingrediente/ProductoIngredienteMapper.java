package com.bubblesessence.ventas.productoingrediente;

import com.bubblesessence.ventas.productoingrediente.dto.ProductoIngredienteResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductoIngredienteMapper {

    public ProductoIngredienteResponseDTO toResponseDTO(ProductoIngrediente entidad) {
        return ProductoIngredienteResponseDTO.builder()
                .id(entidad.getId())
                .productoId(entidad.getProducto().getId())
                .productoNombre(entidad.getProducto().getNombre())
                .ingredienteId(entidad.getIngrediente().getId())
                .ingredienteNombre(entidad.getIngrediente().getNombre())
                .cantidadReferencial(entidad.getCantidadReferencial())
                .build();
    }
}
