package com.bubblesessence.seguridad.accion;

import com.bubblesessence.common.exception.BusinessException;
import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.seguridad.accion.dto.AccionRequestDTO;
import com.bubblesessence.seguridad.accion.dto.AccionResponseDTO;
import com.bubblesessence.seguridad.modulo.Modulo;
import com.bubblesessence.seguridad.modulo.ModuloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccionServiceImpl implements AccionService {

    private final AccionRepository accionRepository;
    private final ModuloRepository moduloRepository;
    private final AccionMapper accionMapper;

    @Override
    public List<AccionResponseDTO> listar(Integer moduloId) {
        List<Accion> acciones = moduloId != null
                ? accionRepository.findByModulo_Id(moduloId)
                : accionRepository.findAll();
        return acciones.stream().map(accionMapper::toResponseDTO).toList();
    }

    @Override
    public AccionResponseDTO obtenerPorId(Integer id) {
        return accionMapper.toResponseDTO(buscarEntidadOFallar(id));
    }

    @Override
    @Transactional
    public AccionResponseDTO crear(AccionRequestDTO request) {
        if (accionRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe una acción con código '%s'".formatted(request.getCodigo()));
        }
        Modulo modulo = moduloRepository.findById(request.getModuloId())
                .orElseThrow(() -> new ResourceNotFoundException("Módulo", request.getModuloId()));

        Accion accion = Accion.builder()
                .modulo(modulo)
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activo(request.getActivo() == null || request.getActivo())
                .build();
        return accionMapper.toResponseDTO(accionRepository.save(accion));
    }

    @Override
    @Transactional
    public AccionResponseDTO actualizar(Integer id, AccionRequestDTO request) {
        Accion accion = buscarEntidadOFallar(id);

        if (!accion.getCodigo().equals(request.getCodigo()) && accionRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe una acción con código '%s'".formatted(request.getCodigo()));
        }
        Modulo modulo = moduloRepository.findById(request.getModuloId())
                .orElseThrow(() -> new ResourceNotFoundException("Módulo", request.getModuloId()));

        accion.setModulo(modulo);
        accion.setCodigo(request.getCodigo());
        accion.setNombre(request.getNombre());
        accion.setDescripcion(request.getDescripcion());
        if (request.getActivo() != null) {
            accion.setActivo(request.getActivo());
        }
        return accionMapper.toResponseDTO(accionRepository.save(accion));
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        Accion accion = buscarEntidadOFallar(id);
        accion.setActivo(false);
        accionRepository.save(accion);
    }

    @Override
    @Transactional
    public void activar(Integer id) {
        Accion accion = buscarEntidadOFallar(id);
        accion.setActivo(true);
        accionRepository.save(accion);
    }

    Accion buscarEntidadOFallar(Integer id) {
        return accionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acción", id));
    }
}
