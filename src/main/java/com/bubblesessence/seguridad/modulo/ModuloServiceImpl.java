package com.bubblesessence.seguridad.modulo;

import com.bubblesessence.common.exception.BusinessException;
import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.seguridad.modulo.dto.ModuloRequestDTO;
import com.bubblesessence.seguridad.modulo.dto.ModuloResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModuloServiceImpl implements ModuloService {

    private final ModuloRepository moduloRepository;
    private final ModuloMapper moduloMapper;

    @Override
    public List<ModuloResponseDTO> listar() {
        return moduloRepository.findAllByOrderByOrdenAsc().stream()
                .map(moduloMapper::toResponseDTO).toList();
    }

    @Override
    public ModuloResponseDTO obtenerPorId(Integer id) {
        return moduloMapper.toResponseDTO(buscarEntidadOFallar(id));
    }

    @Override
    @Transactional
    public ModuloResponseDTO crear(ModuloRequestDTO request) {
        if (moduloRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe un módulo con código '%s'".formatted(request.getCodigo()));
        }
        Modulo modulo = Modulo.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .orden(request.getOrden())
                .activo(request.getActivo() == null || request.getActivo())
                .build();
        return moduloMapper.toResponseDTO(moduloRepository.save(modulo));
    }

    @Override
    @Transactional
    public ModuloResponseDTO actualizar(Integer id, ModuloRequestDTO request) {
        Modulo modulo = buscarEntidadOFallar(id);

        if (!modulo.getCodigo().equals(request.getCodigo()) && moduloRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe un módulo con código '%s'".formatted(request.getCodigo()));
        }

        modulo.setCodigo(request.getCodigo());
        modulo.setNombre(request.getNombre());
        modulo.setDescripcion(request.getDescripcion());
        if (request.getOrden() != null) {
            modulo.setOrden(request.getOrden());
        }
        if (request.getActivo() != null) {
            modulo.setActivo(request.getActivo());
        }
        return moduloMapper.toResponseDTO(moduloRepository.save(modulo));
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        Modulo modulo = buscarEntidadOFallar(id);
        modulo.setActivo(false);
        moduloRepository.save(modulo);
    }

    @Override
    @Transactional
    public void activar(Integer id) {
        Modulo modulo = buscarEntidadOFallar(id);
        modulo.setActivo(true);
        moduloRepository.save(modulo);
    }

    Modulo buscarEntidadOFallar(Integer id) {
        return moduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo", id));
    }
}
