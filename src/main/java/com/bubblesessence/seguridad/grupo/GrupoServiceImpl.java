package com.bubblesessence.seguridad.grupo;

import com.bubblesessence.common.exception.BusinessException;
import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.seguridad.accion.Accion;
import com.bubblesessence.seguridad.accion.AccionMapper;
import com.bubblesessence.seguridad.accion.AccionRepository;
import com.bubblesessence.seguridad.accion.dto.AccionResponseDTO;
import com.bubblesessence.seguridad.grupo.dto.GrupoRequestDTO;
import com.bubblesessence.seguridad.grupo.dto.GrupoResponseDTO;
import com.bubblesessence.seguridad.usuario.Usuario;
import com.bubblesessence.seguridad.usuario.UsuarioMapper;
import com.bubblesessence.seguridad.usuario.UsuarioRepository;
import com.bubblesessence.seguridad.usuario.dto.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrupoServiceImpl implements GrupoService {

    private final GrupoRepository grupoRepository;
    private final GrupoAccionRepository grupoAccionRepository;
    private final UsuarioGrupoRepository usuarioGrupoRepository;
    private final AccionRepository accionRepository;
    private final UsuarioRepository usuarioRepository;

    private final GrupoMapper grupoMapper;
    private final AccionMapper accionMapper;
    private final UsuarioMapper usuarioMapper;

    // ---------------------------------------------------------------- CRUD

    @Override
    public List<GrupoResponseDTO> listar() {
        return grupoRepository.findAll().stream().map(grupoMapper::toResponseDTO).toList();
    }

    @Override
    public GrupoResponseDTO obtenerPorId(Integer id) {
        return grupoMapper.toResponseDTO(buscarGrupoOFallar(id));
    }

    @Override
    @Transactional
    public GrupoResponseDTO crear(GrupoRequestDTO request) {
        if (grupoRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe un grupo con código '%s'".formatted(request.getCodigo()));
        }
        Grupo grupo = Grupo.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activo(request.getActivo() == null || request.getActivo())
                .build();
        return grupoMapper.toResponseDTO(grupoRepository.save(grupo));
    }

    @Override
    @Transactional
    public GrupoResponseDTO actualizar(Integer id, GrupoRequestDTO request) {
        Grupo grupo = buscarGrupoOFallar(id);

        if (!grupo.getCodigo().equals(request.getCodigo()) && grupoRepository.existsByCodigo(request.getCodigo())) {
            throw new BusinessException("Ya existe un grupo con código '%s'".formatted(request.getCodigo()));
        }

        grupo.setCodigo(request.getCodigo());
        grupo.setNombre(request.getNombre());
        grupo.setDescripcion(request.getDescripcion());
        if (request.getActivo() != null) {
            grupo.setActivo(request.getActivo());
        }
        return grupoMapper.toResponseDTO(grupoRepository.save(grupo));
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        Grupo grupo = buscarGrupoOFallar(id);
        grupo.setActivo(false);
        grupoRepository.save(grupo);
    }

    @Override
    @Transactional
    public void activar(Integer id) {
        Grupo grupo = buscarGrupoOFallar(id);
        grupo.setActivo(true);
        grupoRepository.save(grupo);
    }

    // ------------------------------------------------------ acciones del grupo

    @Override
    public List<AccionResponseDTO> listarAcciones(Integer grupoId) {
        buscarGrupoOFallar(grupoId);
        return grupoAccionRepository.findByGrupo_Id(grupoId).stream()
                .map(ga -> accionMapper.toResponseDTO(ga.getAccion()))
                .toList();
    }

    @Override
    @Transactional
    public void asignarAccion(Integer grupoId, Integer accionId) {
        Grupo grupo = buscarGrupoOFallar(grupoId);
        Accion accion = accionRepository.findById(accionId)
                .orElseThrow(() -> new ResourceNotFoundException("Acción", accionId));

        if (grupoAccionRepository.existsByGrupo_IdAndAccion_Id(grupoId, accionId)) {
            throw new BusinessException("El grupo '%s' ya tiene la acción '%s'"
                    .formatted(grupo.getCodigo(), accion.getCodigo()));
        }

        grupoAccionRepository.save(GrupoAccion.builder().grupo(grupo).accion(accion).build());
    }

    @Override
    @Transactional
    public void quitarAccion(Integer grupoId, Integer accionId) {
        GrupoAccion grupoAccion = grupoAccionRepository.findByGrupo_IdAndAccion_Id(grupoId, accionId)
                .orElseThrow(() -> new ResourceNotFoundException("El grupo no tiene esa acción asignada"));
        grupoAccionRepository.delete(grupoAccion);
    }

    // ------------------------------------------------------ usuarios del grupo

    @Override
    public List<UsuarioResponseDTO> listarUsuarios(Integer grupoId) {
        buscarGrupoOFallar(grupoId);
        return usuarioGrupoRepository.findByGrupo_Id(grupoId).stream()
                .map(ug -> usuarioMapper.toResponseDTO(ug.getUsuario()))
                .toList();
    }

    @Override
    @Transactional
    public void asignarUsuario(Integer grupoId, Long usuarioId) {
        Grupo grupo = buscarGrupoOFallar(grupoId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        if (usuarioGrupoRepository.existsByUsuario_IdAndGrupo_Id(usuarioId, grupoId)) {
            throw new BusinessException("El usuario '%s' ya pertenece al grupo '%s'"
                    .formatted(usuario.getUsuario(), grupo.getCodigo()));
        }

        usuarioGrupoRepository.save(UsuarioGrupo.builder().usuario(usuario).grupo(grupo).build());
    }

    @Override
    @Transactional
    public void quitarUsuario(Integer grupoId, Long usuarioId) {
        UsuarioGrupo usuarioGrupo = usuarioGrupoRepository.findByUsuario_IdAndGrupo_Id(usuarioId, grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no pertenece a ese grupo"));
        usuarioGrupoRepository.delete(usuarioGrupo);
    }

    // ---------------------------------------------------------------- helpers

    private Grupo buscarGrupoOFallar(Integer id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo", id));
    }
}
