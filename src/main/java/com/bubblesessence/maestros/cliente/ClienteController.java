package com.bubblesessence.maestros.cliente;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.maestros.cliente.dto.ClienteRequestDTO;
import com.bubblesessence.maestros.cliente.dto.ClienteResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Administración de clientes con cuenta, para uso del staff (ver historial,
 * buscar por teléfono, etc). Crear un Cliente formal es una acción interna;
 * un invitado que hace un pedido NO pasa por acá (ver PedidoController).
 */
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','OPERADOR','VENDEDOR')")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ApiResponse<List<ClienteResponseDTO>> listar() {
        return ApiResponse.success(clienteService.listar());
    }

    @GetMapping("/{id}")
    public ApiResponse<ClienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ApiResponse.success(clienteService.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO request) {
        return ApiResponse.success("Cliente creado correctamente", clienteService.crear(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ClienteResponseDTO> actualizar(
            @PathVariable Long id, @Valid @RequestBody ClienteRequestDTO request) {
        return ApiResponse.success("Cliente actualizado correctamente", clienteService.actualizar(id, request));
    }
}
