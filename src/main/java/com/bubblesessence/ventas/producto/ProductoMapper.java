package com.bubblesessence.ventas.producto;

import com.bubblesessence.ventas.producto.dto.ProductoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoResponseDTO toResponseDTO(Producto producto) {
        return ProductoResponseDTO.builder()
                .id(producto.getId())
                .codigo(producto.getCodigo())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .imagenUrl(producto.getImagenUrl())
                .activo(producto.getActivo())
                .fechaCreacion(producto.getFechaCreacion())
                .usuarioCreador(producto.getUsuarioCreador())
                .build();
    }
}
