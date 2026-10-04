package com.bubblesessence.maestros.cliente;

import com.bubblesessence.maestros.cliente.dto.ClienteResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .nombreCompleto(cliente.getNombreCompleto())
                .telefono(cliente.getTelefono())
                .correo(cliente.getCorreo())
                .direccion(cliente.getDireccion())
                .instagram(cliente.getInstagram())
                .fechaRegistro(cliente.getFechaRegistro())
                .build();
    }
}
