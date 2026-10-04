package com.bubblesessence.seguridad.acceso;

import com.bubblesessence.common.exception.InvalidCredentialsException;
import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.seguridad.acceso.dto.AccesoResponseDTO;
import com.bubblesessence.seguridad.auth.UsuarioPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint que consume el front para saber qué mostrar: a qué grupos
 * pertenece el usuario logueado, a qué módulos entra, y qué acciones puede
 * ejecutar (los códigos tipo "btn-editar-pedido"). Es de solo lectura;
 * todo el mantenimiento de grupos/acciones/permisos vive en
 * GrupoController, AccionController, ModuloController y PermisoController.
 */
@RestController
@RequestMapping("/api/v1/accesos")
@RequiredArgsConstructor
public class AccesoController {

    private final AccesoService accesoService;

    /** El propio usuario logueado consulta sus accesos (cualquier rol, con solo estar autenticado). */
    @GetMapping("/mis-accesos")
    public ApiResponse<AccesoResponseDTO> misAccesos(Authentication authentication) {
        Long usuarioId = idDelUsuarioLogueado(authentication);
        return ApiResponse.success(accesoService.resolverAcceso(usuarioId));
    }

    /** Un ADMIN inspecciona/simula los accesos de cualquier otro usuario (útil para la pantalla de administración de permisos). */
    @GetMapping("/usuarios/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AccesoResponseDTO> accesosDeUsuario(@PathVariable Long usuarioId) {
        return ApiResponse.success(accesoService.resolverAcceso(usuarioId));
    }

    private Long idDelUsuarioLogueado(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getId();
        }
        // No debería pasar nunca (este endpoint exige autenticación en SecurityConfig),
        // pero si llega acá sin un UsuarioPrincipal válido, se corta seco.
        throw new InvalidCredentialsException("No se pudo identificar al usuario autenticado");
    }
}
