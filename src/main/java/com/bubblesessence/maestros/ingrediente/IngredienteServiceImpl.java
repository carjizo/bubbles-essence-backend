package com.bubblesessence.maestros.ingrediente;

import com.bubblesessence.common.exception.BusinessException;
import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.maestros.ingrediente.dto.IngredienteRequestDTO;
import com.bubblesessence.maestros.ingrediente.dto.IngredienteResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredienteServiceImpl implements IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final IngredienteMapper ingredienteMapper;

    @Override
    public List<IngredienteResponseDTO> listar(Boolean activo) {
        List<Ingrediente> ingredientes = (activo != null)
                ? ingredienteRepository.findByActivo(activo)
                : ingredienteRepository.findAll();
        return ingredientes.stream().map(ingredienteMapper::toResponseDTO).toList();
    }

    @Override
    public IngredienteResponseDTO obtenerPorId(Integer id) {
        return ingredienteMapper.toResponseDTO(buscarEntidadOFallar(id));
    }

    @Override
    @Transactional
    public IngredienteResponseDTO crear(IngredienteRequestDTO request) {
        if (ingredienteRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException("Ya existe un ingrediente con el nombre '%s'".formatted(request.getNombre()));
        }

        Ingrediente ingrediente = Ingrediente.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activo(request.getActivo() == null || request.getActivo())
                .build();

        return ingredienteMapper.toResponseDTO(ingredienteRepository.save(ingrediente));
    }

    @Override
    @Transactional
    public IngredienteResponseDTO actualizar(Integer id, IngredienteRequestDTO request) {
        Ingrediente ingrediente = buscarEntidadOFallar(id);

        if (!ingrediente.getNombre().equalsIgnoreCase(request.getNombre())
                && ingredienteRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException("Ya existe un ingrediente con el nombre '%s'".formatted(request.getNombre()));
        }

        ingrediente.setNombre(request.getNombre());
        ingrediente.setDescripcion(request.getDescripcion());
        if (request.getActivo() != null) {
            ingrediente.setActivo(request.getActivo());
        }

        return ingredienteMapper.toResponseDTO(ingredienteRepository.save(ingrediente));
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        Ingrediente ingrediente = buscarEntidadOFallar(id);
        ingrediente.setActivo(false);
        ingredienteRepository.save(ingrediente);
    }

    private Ingrediente buscarEntidadOFallar(Integer id) {
        return ingredienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente", id));
    }
}
