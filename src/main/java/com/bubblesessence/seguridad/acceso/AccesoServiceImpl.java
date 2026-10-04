package com.bubblesessence.seguridad.acceso;

import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.seguridad.acceso.dto.AccesoResponseDTO;
import com.bubblesessence.seguridad.acceso.dto.GrupoResumenDTO;
import com.bubblesessence.seguridad.acceso.dto.ModuloResumenDTO;
import com.bubblesessence.seguridad.accion.Accion;
import com.bubblesessence.seguridad.grupo.Grupo;
import com.bubblesessence.seguridad.grupo.GrupoAccion;
import com.bubblesessence.seguridad.grupo.GrupoAccionRepository;
import com.bubblesessence.seguridad.grupo.UsuarioGrupo;
import com.bubblesessence.seguridad.grupo.UsuarioGrupoRepository;
import com.bubblesessence.seguridad.modulo.Modulo;
import com.bubblesessence.seguridad.permiso.TipoPermiso;
import com.bubblesessence.seguridad.permiso.UsuarioAccion;
import com.bubblesessence.seguridad.permiso.UsuarioAccionRepository;
import com.bubblesessence.seguridad.usuario.Usuario;
import com.bubblesessence.seguridad.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccesoServiceImpl implements AccesoService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioGrupoRepository usuarioGrupoRepository;
    private final GrupoAccionRepository grupoAccionRepository;
    private final UsuarioAccionRepository usuarioAccionRepository;

    @Override
    public AccesoResponseDTO resolverAcceso(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        // 1) Grupos activos del usuario
        List<Grupo> grupos = usuarioGrupoRepository.findByUsuario_Id(usuarioId).stream()
                .map(UsuarioGrupo::getGrupo)
                .filter(g -> Boolean.TRUE.equals(g.getActivo()))
                .toList();

        // 2) Unión de las acciones de esos grupos (activas), dedupe por id de acción
        Map<Integer, Accion> acciones = new LinkedHashMap<>();
        if (!grupos.isEmpty()) {
            List<Integer> grupoIds = grupos.stream().map(Grupo::getId).toList();
            for (GrupoAccion ga : grupoAccionRepository.findByGrupo_IdIn(grupoIds)) {
                Accion accion = ga.getAccion();
                if (Boolean.TRUE.equals(accion.getActivo())) {
                    acciones.put(accion.getId(), accion);
                }
            }
        }

        // 3) Overrides puntuales del usuario: GRANT suma, DENY resta (siempre gana el override)
        for (UsuarioAccion override : usuarioAccionRepository.findByUsuario_Id(usuarioId)) {
            Accion accion = override.getAccion();
            if (override.getTipo() == TipoPermiso.DENY) {
                acciones.remove(accion.getId());
            } else if (Boolean.TRUE.equals(accion.getActivo())) {
                acciones.put(accion.getId(), accion);
            }
        }

        // 4) Módulos = derivados de las acciones efectivas (dedupe por id de módulo)
        Map<Integer, Modulo> modulos = new LinkedHashMap<>();
        for (Accion accion : acciones.values()) {
            Modulo modulo = accion.getModulo();
            if (Boolean.TRUE.equals(modulo.getActivo())) {
                modulos.put(modulo.getId(), modulo);
            }
        }

        // 4.b) Acciones agrupadas por módulo (códigos). Esto permite al front
        // saber para CADA módulo qué acciones están habilitadas sin tener
        // que inferir la relación desde un catálogo separado.
        Map<Integer, java.util.List<String>> accionesPorModulo = new LinkedHashMap<>();
        for (Accion accion : acciones.values()) {
            Modulo modulo = accion.getModulo();
            if (modulo == null) continue;
            if (!Boolean.TRUE.equals(modulo.getActivo())) continue;
            accionesPorModulo.computeIfAbsent(modulo.getId(), k -> new java.util.ArrayList<>())
                    .add(accion.getCodigo());
        }

        return AccesoResponseDTO.builder()
                .usuarioId(usuario.getId())
                .usuario(usuario.getUsuario())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .grupos(grupos.stream()
                        .map(g -> GrupoResumenDTO.builder().id(g.getId()).codigo(g.getCodigo()).nombre(g.getNombre()).build())
                        .toList())
                .modulos(modulos.values().stream()
                    .sorted(Comparator.comparing(m -> m.getOrden() == null ? 0 : m.getOrden()))
                    .map(m -> ModuloResumenDTO.builder()
                        .id(m.getId())
                        .codigo(m.getCodigo())
                        .nombre(m.getNombre())
                        .orden(m.getOrden())
                        .acciones(accionesPorModulo.getOrDefault(m.getId(), java.util.List.of()))
                        .build())
                    .toList())
                .acciones(acciones.values().stream().map(Accion::getCodigo).toList())
                .build();
    }
}
