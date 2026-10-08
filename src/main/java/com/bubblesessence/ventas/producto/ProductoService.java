package com.bubblesessence.ventas.producto;

import com.bubblesessence.ventas.producto.dto.ProductoFiltroDTO;
import com.bubblesessence.ventas.producto.dto.ProductoRequestDTO;
import com.bubblesessence.ventas.producto.dto.ProductoResponseDTO;

import java.util.List;

public interface ProductoService {

    List<ProductoResponseDTO> listar(ProductoFiltroDTO filtro);

    ProductoResponseDTO obtenerPorId(Integer id);

    ProductoResponseDTO crear(ProductoRequestDTO request);

    ProductoResponseDTO actualizar(Integer id, ProductoRequestDTO request);

    void desactivar(Integer id);
}
