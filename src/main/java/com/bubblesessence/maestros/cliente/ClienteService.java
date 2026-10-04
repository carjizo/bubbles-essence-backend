package com.bubblesessence.maestros.cliente;

import com.bubblesessence.maestros.cliente.dto.ClienteRequestDTO;
import com.bubblesessence.maestros.cliente.dto.ClienteResponseDTO;

import java.util.List;

public interface ClienteService {

    List<ClienteResponseDTO> listar();

    ClienteResponseDTO obtenerPorId(Long id);

    ClienteResponseDTO crear(ClienteRequestDTO request);

    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO request);
}
