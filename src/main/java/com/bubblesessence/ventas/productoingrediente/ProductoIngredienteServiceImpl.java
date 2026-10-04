package com.bubblesessence.ventas.productoingrediente;

import com.bubblesessence.common.exception.BusinessException;
import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.maestros.ingrediente.Ingrediente;
import com.bubblesessence.maestros.ingrediente.IngredienteRepository;
import com.bubblesessence.ventas.producto.Producto;
import com.bubblesessence.ventas.producto.ProductoRepository;
import com.bubblesessence.ventas.productoingrediente.dto.ProductoIngredienteRequestDTO;
import com.bubblesessence.ventas.productoingrediente.dto.ProductoIngredienteResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoIngredienteServiceImpl implements ProductoIngredienteService {

    private final ProductoIngredienteRepository productoIngredienteRepository;
    private final ProductoRepository productoRepository;
    private final IngredienteRepository ingredienteRepository;
    private final ProductoIngredienteMapper productoIngredienteMapper;

    @Override
    public List<ProductoIngredienteResponseDTO> listarPorProducto(Integer productoId) {
        List<ProductoIngrediente> receta = (productoId != null)
                ? productoIngredienteRepository.findByProducto_Id(productoId)
                : productoIngredienteRepository.findAll();
        return receta.stream().map(productoIngredienteMapper::toResponseDTO).toList();
    }

    @Override
    public ProductoIngredienteResponseDTO obtenerPorId(Long id) {
        return productoIngredienteMapper.toResponseDTO(buscarEntidadOFallar(id));
    }

    @Override
    @Transactional
    public ProductoIngredienteResponseDTO crear(ProductoIngredienteRequestDTO request) {
        Producto producto = buscarProductoOFallar(request.getProductoId());
        Ingrediente ingrediente = buscarIngredienteOFallar(request.getIngredienteId());

        if (productoIngredienteRepository.existsByProducto_IdAndIngrediente_Id(
                request.getProductoId(), request.getIngredienteId())) {
            throw new BusinessException(
                    "El producto '%s' ya tiene registrado el ingrediente '%s' en su receta"
                            .formatted(producto.getNombre(), ingrediente.getNombre()));
        }

        ProductoIngrediente entidad = ProductoIngrediente.builder()
                .producto(producto)
                .ingrediente(ingrediente)
                .cantidadReferencial(request.getCantidadReferencial())
                .build();

        return productoIngredienteMapper.toResponseDTO(productoIngredienteRepository.save(entidad));
    }

    @Override
    @Transactional
    public ProductoIngredienteResponseDTO actualizar(Long id, ProductoIngredienteRequestDTO request) {
        ProductoIngrediente entidad = buscarEntidadOFallar(id);
        Producto producto = buscarProductoOFallar(request.getProductoId());
        Ingrediente ingrediente = buscarIngredienteOFallar(request.getIngredienteId());

        boolean cambiaCombinacion = !entidad.getProducto().getId().equals(request.getProductoId())
                || !entidad.getIngrediente().getId().equals(request.getIngredienteId());

        if (cambiaCombinacion && productoIngredienteRepository.existsByProducto_IdAndIngrediente_Id(
                request.getProductoId(), request.getIngredienteId())) {
            throw new BusinessException(
                    "El producto '%s' ya tiene registrado el ingrediente '%s' en su receta"
                            .formatted(producto.getNombre(), ingrediente.getNombre()));
        }

        entidad.setProducto(producto);
        entidad.setIngrediente(ingrediente);
        entidad.setCantidadReferencial(request.getCantidadReferencial());

        return productoIngredienteMapper.toResponseDTO(productoIngredienteRepository.save(entidad));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        ProductoIngrediente entidad = buscarEntidadOFallar(id);
        productoIngredienteRepository.delete(entidad);
    }

    private ProductoIngrediente buscarEntidadOFallar(Long id) {
        return productoIngredienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receta de producto-ingrediente", id));
    }

    private Producto buscarProductoOFallar(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    private Ingrediente buscarIngredienteOFallar(Integer id) {
        return ingredienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente", id));
    }
}
