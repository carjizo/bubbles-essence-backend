package com.bubblesessence.ventas.producto;

import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.ventas.producto.dto.ProductoFiltroDTO;
import com.bubblesessence.ventas.producto.dto.ProductoRequestDTO;
import com.bubblesessence.ventas.producto.dto.ProductoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Trae hasta 50 productos (ver ProductoServiceImpl.LIMITE_FILAS),
     * ordenados por fecha de creación descendente, con filtros opcionales
     * combinables. El front pagina esas filas localmente, no manda
     * page/size acá.
     *
     * - ingrediente: busca productos que tengan un ingrediente cuyo
     *   nombre contenga el texto (ej. "mie" -> jabones con "Miel").
     */
    @GetMapping
    public ApiResponse<List<ProductoResponseDTO>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String ingrediente,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre) {
        ProductoFiltroDTO filtro = new ProductoFiltroDTO(activo, fechaDesde, fechaHasta, ingrediente, codigo, nombre);
        return ApiResponse.success(productoService.listar(filtro));
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
