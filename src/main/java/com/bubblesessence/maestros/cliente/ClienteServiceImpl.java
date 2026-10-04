package com.bubblesessence.maestros.cliente;

import com.bubblesessence.common.exception.BusinessException;
import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.maestros.cliente.dto.ClienteRequestDTO;
import com.bubblesessence.maestros.cliente.dto.ClienteResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    public List<ClienteResponseDTO> listar() {
        return clienteRepository.findAll().stream().map(clienteMapper::toResponseDTO).toList();
    }

    @Override
    public ClienteResponseDTO obtenerPorId(Long id) {
        return clienteMapper.toResponseDTO(buscarEntidadOFallar(id));
    }

    @Override
    @Transactional
    public ClienteResponseDTO crear(ClienteRequestDTO request) {
        if (clienteRepository.existsByTelefono(request.getTelefono())) {
            throw new BusinessException("Ya existe un cliente con el teléfono '%s'".formatted(request.getTelefono()));
        }

        Cliente cliente = Cliente.builder()
                .nombreCompleto(request.getNombreCompleto())
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .direccion(request.getDireccion())
                .instagram(request.getInstagram())
                .build();

        return clienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteRequestDTO request) {
        Cliente cliente = buscarEntidadOFallar(id);

        if (!cliente.getTelefono().equals(request.getTelefono())
                && clienteRepository.existsByTelefono(request.getTelefono())) {
            throw new BusinessException("Ya existe un cliente con el teléfono '%s'".formatted(request.getTelefono()));
        }

        cliente.setNombreCompleto(request.getNombreCompleto());
        cliente.setTelefono(request.getTelefono());
        cliente.setCorreo(request.getCorreo());
        cliente.setDireccion(request.getDireccion());
        cliente.setInstagram(request.getInstagram());

        return clienteMapper.toResponseDTO(clienteRepository.save(cliente));
    }

    Cliente buscarEntidadOFallar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}
