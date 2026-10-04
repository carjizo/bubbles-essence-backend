package com.bubblesessence.ventas.productoingrediente;

import com.bubblesessence.ventas.productoingrediente.dto.ProductoIngredienteRequestDTO;
import com.bubblesessence.ventas.productoingrediente.dto.ProductoIngredienteResponseDTO;

import java.util.List;

public interface ProductoIngredienteService {

    List<ProductoIngredienteResponseDTO> listarPorProducto(Integer productoId);

    ProductoIngredienteResponseDTO obtenerPorId(Long id);

    ProductoIngredienteResponseDTO crear(ProductoIngredienteRequestDTO request);

    ProductoIngredienteResponseDTO actualizar(Long id, ProductoIngredienteRequestDTO request);

    void eliminar(Long id);
}
