package com.bubblesessence.ventas.producto;

import com.bubblesessence.common.exception.BusinessException;
import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.ventas.producto.dto.ProductoRequestDTO;
import com.bubblesessence.ventas.producto.dto.ProductoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    @Override
    public List<ProductoResponseDTO> listar(Boolean activo) {
        List<Producto> productos = (activo != null)
                ? productoRepository.findByActivo(activo)
                : productoRepository.findAll();
        return productos.stream().map(productoMapper::toResponseDTO).toList();
    }

    @Override
    public ProductoResponseDTO obtenerPorId(Integer id) {
        return productoMapper.toResponseDTO(buscarEntidadOFallar(id));
    }

    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO request) {
        if (productoRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException("Ya existe un producto con el nombre '%s'".formatted(request.getNombre()));
        }
        if (productoRepository.existsByCodigoIgnoreCase(request.getCodigo())) {
            throw new BusinessException("Ya existe un producto con el código '%s'".formatted(request.getCodigo()));
        }

        Producto producto = Producto.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .activo(request.getActivo() == null || request.getActivo())
                .build();

        return productoMapper.toResponseDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Integer id, ProductoRequestDTO request) {
        Producto producto = buscarEntidadOFallar(id);

        if (!producto.getNombre().equalsIgnoreCase(request.getNombre())
                && productoRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException("Ya existe un producto con el nombre '%s'".formatted(request.getNombre()));
        }

        if (!producto.getCodigo().equalsIgnoreCase(request.getCodigo())
                && productoRepository.existsByCodigoIgnoreCase(request.getCodigo())) {
            throw new BusinessException("Ya existe un producto con el código '%s'".formatted(request.getCodigo()));
        }

        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        if (request.getStock() != null) {
            producto.setStock(request.getStock());
        }
        if (request.getActivo() != null) {
            producto.setActivo(request.getActivo());
        }

        return productoMapper.toResponseDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        Producto producto = buscarEntidadOFallar(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private Producto buscarEntidadOFallar(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }
}
