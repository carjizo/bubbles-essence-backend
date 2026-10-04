package com.bubblesessence.seguridad.permiso;

import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.seguridad.accion.Accion;
import com.bubblesessence.seguridad.accion.AccionRepository;
import com.bubblesessence.seguridad.permiso.dto.AsignarPermisoRequestDTO;
import com.bubblesessence.seguridad.permiso.dto.PermisoResponseDTO;
import com.bubblesessence.seguridad.usuario.Usuario;
import com.bubblesessence.seguridad.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermisoServiceImpl implements PermisoService {

    private final UsuarioAccionRepository usuarioAccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AccionRepository accionRepository;

    @Override
    public List<PermisoResponseDTO> listarPorUsuario(Long usuarioId) {
        return usuarioAccionRepository.findByUsuario_Id(usuarioId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PermisoResponseDTO asignar(Long usuarioId, AsignarPermisoRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));
        Accion accion = accionRepository.findById(request.getAccionId())
                .orElseThrow(() -> new ResourceNotFoundException("Acción", request.getAccionId()));

        // Si ya existe un override para este usuario+acción, se actualiza el tipo
        // en vez de duplicarlo (permite corregir GRANT<->DENY sin borrar antes).
        UsuarioAccion usuarioAccion = usuarioAccionRepository
                .findByUsuario_IdAndAccion_Id(usuarioId, request.getAccionId())
                .orElseGet(() -> UsuarioAccion.builder().usuario(usuario).accion(accion).build());

        usuarioAccion.setTipo(request.getTipo());

        return toResponseDTO(usuarioAccionRepository.save(usuarioAccion));
    }

    @Override
    @Transactional
    public void quitar(Long usuarioId, Integer accionId) {
        UsuarioAccion usuarioAccion = usuarioAccionRepository.findByUsuario_IdAndAccion_Id(usuarioId, accionId)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no tiene ese permiso puntual asignado"));
        usuarioAccionRepository.delete(usuarioAccion);
    }

    private PermisoResponseDTO toResponseDTO(UsuarioAccion ua) {
        return PermisoResponseDTO.builder()
                .id(ua.getId())
                .accionId(ua.getAccion().getId())
                .accionCodigo(ua.getAccion().getCodigo())
                .accionNombre(ua.getAccion().getNombre())
                .tipo(ua.getTipo())
                .build();
    }
}
