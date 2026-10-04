package com.bubblesessence.ventas.producto;

import com.bubblesessence.ventas.producto.dto.ProductoRequestDTO;
import com.bubblesessence.ventas.producto.dto.ProductoResponseDTO;

import java.util.List;

public interface ProductoService {

    List<ProductoResponseDTO> listar(Boolean activo);

    ProductoResponseDTO obtenerPorId(Integer id);

    ProductoResponseDTO crear(ProductoRequestDTO request);

    ProductoResponseDTO actualizar(Integer id, ProductoRequestDTO request);

    void desactivar(Integer id);
}
