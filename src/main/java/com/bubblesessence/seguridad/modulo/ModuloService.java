package com.bubblesessence.seguridad.modulo;

import com.bubblesessence.seguridad.modulo.dto.ModuloRequestDTO;
import com.bubblesessence.seguridad.modulo.dto.ModuloResponseDTO;

import java.util.List;

public interface ModuloService {

    List<ModuloResponseDTO> listar();

    ModuloResponseDTO obtenerPorId(Integer id);

    ModuloResponseDTO crear(ModuloRequestDTO request);

    ModuloResponseDTO actualizar(Integer id, ModuloRequestDTO request);

    void desactivar(Integer id);

    void activar(Integer id);
}
