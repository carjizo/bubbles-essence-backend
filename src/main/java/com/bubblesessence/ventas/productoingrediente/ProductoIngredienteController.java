package com.bubblesessence.ventas.productoingrediente;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.ventas.productoingrediente.dto.ProductoIngredienteRequestDTO;
import com.bubblesessence.ventas.productoingrediente.dto.ProductoIngredienteResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone la "receta" de cada jabón: qué ingredientes lleva.
 * Ej: GET /api/v1/producto-ingredientes?productoId=3
 */
@RestController
@RequestMapping("/api/v1/producto-ingredientes")
@RequiredArgsConstructor
public class ProductoIngredienteController {

    private final ProductoIngredienteService productoIngredienteService;

    @GetMapping
    public ApiResponse<List<ProductoIngredienteResponseDTO>> listar(
            @RequestParam(required = false) Integer productoId) {
        return ApiResponse.success(productoIngredienteService.listarPorProducto(productoId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductoIngredienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ApiResponse.success(productoIngredienteService.obtenerPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductoIngredienteResponseDTO> crear(
            @Valid @RequestBody ProductoIngredienteRequestDTO request) {
        return ApiResponse.success("Ingrediente agregado a la receta correctamente",
                productoIngredienteService.crear(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    @PutMapping("/{id}")
    public ApiResponse<ProductoIngredienteResponseDTO> actualizar(
            @PathVariable Long id, @Valid @RequestBody ProductoIngredienteRequestDTO request) {
        return ApiResponse.success("Receta actualizada correctamente",
                productoIngredienteService.actualizar(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> eliminar(@PathVariable Long id) {
        productoIngredienteService.eliminar(id);
        return ApiResponse.success("Ingrediente removido de la receta correctamente", null);
    }
}
