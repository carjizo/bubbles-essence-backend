package com.bubblesessence.maestros.ingrediente;

import com.bubblesessence.maestros.ingrediente.dto.IngredienteRequestDTO;
import com.bubblesessence.maestros.ingrediente.dto.IngredienteResponseDTO;

import java.util.List;

public interface IngredienteService {

    List<IngredienteResponseDTO> listar(Boolean activo);

    IngredienteResponseDTO obtenerPorId(Integer id);

    IngredienteResponseDTO crear(IngredienteRequestDTO request);

    IngredienteResponseDTO actualizar(Integer id, IngredienteRequestDTO request);

    void desactivar(Integer id);
}
