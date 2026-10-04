package com.bubblesessence.maestros.ingrediente;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.maestros.ingrediente.dto.IngredienteRequestDTO;
import com.bubblesessence.maestros.ingrediente.dto.IngredienteResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredientes")
@RequiredArgsConstructor
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @GetMapping
    public ApiResponse<List<IngredienteResponseDTO>> listar(
            @RequestParam(required = false) Boolean activo) {
        return ApiResponse.success(ingredienteService.listar(activo));
    }

    @GetMapping("/{id}")
    public ApiResponse<IngredienteResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ApiResponse.success(ingredienteService.obtenerPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IngredienteResponseDTO> crear(@Valid @RequestBody IngredienteRequestDTO request) {
        return ApiResponse.success("Ingrediente creado correctamente", ingredienteService.crear(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    @PutMapping("/{id}")
    public ApiResponse<IngredienteResponseDTO> actualizar(
            @PathVariable Integer id, @Valid @RequestBody IngredienteRequestDTO request) {
        return ApiResponse.success("Ingrediente actualizado correctamente", ingredienteService.actualizar(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> desactivar(@PathVariable Integer id) {
        ingredienteService.desactivar(id);
        return ApiResponse.success("Ingrediente desactivado correctamente", null);
    }
}
