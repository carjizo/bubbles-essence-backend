package com.bubblesessence.ventas.pedido;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.seguridad.auth.ClientePrincipal;
import com.bubblesessence.seguridad.auth.UsuarioPrincipal;
import com.bubblesessence.ventas.pedido.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dos mundos, un solo controller:
 *
 * - PÚBLICO (invitado, sin login): crear pedido, ver su propio pedido y
 *   cancelarlo, identificándose con código + teléfono (nunca con el id
 *   interno). Ver qué rutas son públicas en SecurityConfig.
 * - INTERNO (staff logueado): listar todo, ver cualquier pedido, cambiar
 *   estado, cancelar por motivos operativos. Restringido por rol con
 *   @PreAuthorize.
 */
@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    // ---------- INVITADO (público) ----------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PedidoResponseDTO> crear(
            @Valid @RequestBody PedidoRequestDTO request, Authentication authentication) {
        Long vendedorId = idDelUsuarioLogueado(authentication);
        return ApiResponse.success("Pedido creado correctamente", pedidoService.crear(request, vendedorId));
    }

    @GetMapping("/seguimiento")
    public ApiResponse<PedidoResponseDTO> seguimiento(
            @RequestParam String codigoPedido, @RequestParam String documento) {
        return ApiResponse.success(pedidoService.obtenerPublicoPorCodigoYDocumento(codigoPedido, documento));
    }

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('CLIENTE')")
    public ApiResponse<List<PedidoResponseDTO>> misPedidos(Authentication authentication) {
        Long clienteId = idDelClienteLogueado(authentication);
        return ApiResponse.success(pedidoService.obtenerPorCliente(clienteId));
    }

    @PatchMapping("/seguimiento/cancelar")
    public ApiResponse<PedidoResponseDTO> cancelarComoInvitado(
            @Valid @RequestBody SeguimientoCancelarRequestDTO request) {
        return ApiResponse.success("Pedido cancelado correctamente",
                pedidoService.cancelarPublico(request.getCodigoPedido(), request.getDocumento()));
    }

    // ---------- STAFF (requiere login) ----------

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR','VENDEDOR')")
    public ApiResponse<List<PedidoResponseDTO>> listar(
            @RequestParam(required = false) EstadoPedido estado) {
        return ApiResponse.success(pedidoService.listar(estado));
    }

    @GetMapping("/buscar-por-codigo")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ApiResponse<PedidoResponseDTO> buscarPorCodigo(@RequestParam String codigo) {
        return ApiResponse.success(pedidoService.obtenerPorCodigoParaAdmin(codigo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR','VENDEDOR')")
    public ApiResponse<PedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ApiResponse.success(pedidoService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ApiResponse<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody CambiarEstadoPedidoRequestDTO request) {
        return ApiResponse.success("Estado del pedido actualizado", pedidoService.cambiarEstado(id, request));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ApiResponse<PedidoResponseDTO> cancelar(
            @PathVariable Long id, @Valid @RequestBody CancelarPedidoRequestDTO request,
            Authentication authentication) {
        CanceladoPor canceladoPor = tieneRol(authentication, "ADMIN") ? CanceladoPor.ADMIN : CanceladoPor.OPERATOR;
        return ApiResponse.success("Pedido cancelado correctamente",
                pedidoService.cancelar(id, request, canceladoPor));
    }

    // ---------- OPCIÓN A - RÁPIDO: Confirmación de Pago ----------

    /**
     * Confirmar pago de un pedido (PENDING_PAYMENT → PAID).
     * El trabajador verifica el comprobante en Yape y confirma aquí.
     * Se consume el stock en este momento.
     */
    @PatchMapping("/{id}/confirmar-pago")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ApiResponse<PedidoResponseDTO> confirmarPago(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarPagoPedidoRequestDTO request,
            Authentication authentication) {
        Long usuarioId = idDelUsuarioLogueado(authentication);
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        return ApiResponse.success(
            "Pago confirmado correctamente. Stock consumido.",
            pedidoService.confirmarPago(id, request, usuarioId)
        );
    }

    /**
     * Rechazar pago de un pedido.
     * El trabajador detecta comprobante falso o monto no coincide.
     * El pedido sigue en PENDING_PAYMENT, permitiendo reintentar.
     */
    @PatchMapping("/{id}/rechazar-pago")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ApiResponse<PedidoResponseDTO> rechazarPago(
            @PathVariable Long id,
            @Valid @RequestBody RechazarPagoPedidoRequestDTO request,
            Authentication authentication) {
        Long usuarioId = idDelUsuarioLogueado(authentication);
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        return ApiResponse.success(
            "Pago rechazado. El cliente debe reenviar el comprobante.",
            pedidoService.rechazarPago(id, request, usuarioId)
        );
    }

    // ---------- helpers ----------

    /** null si el request viene de un invitado sin token (o con un token inválido). */
    private Long idDelUsuarioLogueado(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getId();
        }
        return null;
    }

    /** Extrae el ID del cliente logueado desde el JWT (tipo CLIENTE) */
    private Long idDelClienteLogueado(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getId();
        }
        if (authentication != null && authentication.getPrincipal() instanceof ClientePrincipal clientePrincipal) {
            return clientePrincipal.getId();
        }
        return null;
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }
}
