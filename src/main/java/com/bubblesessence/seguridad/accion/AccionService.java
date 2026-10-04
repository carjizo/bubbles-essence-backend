package com.bubblesessence.seguridad.accion;

import com.bubblesessence.seguridad.accion.dto.AccionRequestDTO;
import com.bubblesessence.seguridad.accion.dto.AccionResponseDTO;

import java.util.List;

public interface AccionService {

    List<AccionResponseDTO> listar(Integer moduloId);

    AccionResponseDTO obtenerPorId(Integer id);

    AccionResponseDTO crear(AccionRequestDTO request);

    AccionResponseDTO actualizar(Integer id, AccionRequestDTO request);

    void desactivar(Integer id);

    void activar(Integer id);
}
