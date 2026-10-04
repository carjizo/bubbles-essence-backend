package com.bubblesessence.ventas.producto;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.ventas.producto.dto.ProductoRequestDTO;
import com.bubblesessence.ventas.producto.dto.ProductoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ApiResponse<List<ProductoResponseDTO>> listar(
            @RequestParam(required = false) Boolean activo) {
        return ApiResponse.success(productoService.listar(activo));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductoResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ApiResponse.success(productoService.obtenerPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO request) {
        return ApiResponse.success("Producto creado correctamente", productoService.crear(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    @PutMapping("/{id}")
    public ApiResponse<ProductoResponseDTO> actualizar(
            @PathVariable Integer id, @Valid @RequestBody ProductoRequestDTO request) {
        return ApiResponse.success("Producto actualizado correctamente", productoService.actualizar(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> desactivar(@PathVariable Integer id) {
        productoService.desactivar(id);
        return ApiResponse.success("Producto desactivado correctamente", null);
    }
}
