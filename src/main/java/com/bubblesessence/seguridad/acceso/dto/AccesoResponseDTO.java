package com.bubblesessence.seguridad.acceso.dto;

import com.bubblesessence.seguridad.usuario.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Lo que el front (Angular) necesita para pintar el menú y mostrar/ocultar
 * botones, ya resuelto: grupos del usuario, módulos a los que entra, y la
 * lista plana de códigos de acción (ej. "btn-editar-pedido") que puede
 * ejecutar. El front solo hace `acciones.includes('btn-editar-pedido')`,
 * no necesita saber nada de grupos ni de reglas de negocio.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccesoResponseDTO {

    private Long usuarioId;
    private String usuario;
    private String nombreCompleto;
    private RolUsuario rol;
    private Boolean activo;
    private List<GrupoResumenDTO> grupos;
    private List<ModuloResumenDTO> modulos;
    private List<String> acciones;
}
