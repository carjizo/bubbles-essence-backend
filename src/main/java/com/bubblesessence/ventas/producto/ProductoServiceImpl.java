package com.bubblesessence.ventas.producto;

import com.bubblesessence.common.exception.BusinessException;
import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.ventas.producto.dto.ProductoFiltroDTO;
import com.bubblesessence.ventas.producto.dto.ProductoRequestDTO;
import com.bubblesessence.ventas.producto.dto.ProductoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    /** Tope de filas que se trae en una sola consulta; el front pagina
     *  esas filas localmente (ver conversación sobre el patrón de
     *  paginación simple), no se manda page/size desde el cliente. */
    private static final int LIMITE_FILAS = 50;

    /** Por debajo de esto, la búsqueda por ingrediente se ignora en vez
     *  de filtrar -> evita un LIKE demasiado amplio, con muchos falsos positivos,
     *  y protege de scans caros con 1-2 caracteres. El front ya solo
     *  dispara la búsqueda con 3+, esto es la misma regla reforzada acá. */
    private static final int MIN_CARACTERES_BUSQUEDA = 3;

    @Override
    public List<ProductoResponseDTO> listar(ProductoFiltroDTO filtro) {
        String ingrediente = filtro.ingrediente();
        String terminoValido = (ingrediente != null && ingrediente.trim().length() >= MIN_CARACTERES_BUSQUEDA)
                ? ingrediente
                : null;

        Specification<Producto> filtros = Specification
                .where(ProductoSpecifications.activoEs(filtro.activo()))
                .and(ProductoSpecifications.creadoDesde(filtro.fechaDesde()))
                .and(ProductoSpecifications.creadoHasta(filtro.fechaHasta()))
                .and(ProductoSpecifications.codigoContiene(filtro.codigo()))
                .and(ProductoSpecifications.nombreContiene(filtro.nombre()))
                .and(ProductoSpecifications.contieneIngrediente(terminoValido));

        PageRequest limiteOrdenado = PageRequest.of(0, LIMITE_FILAS, Sort.by(Sort.Direction.DESC, "fechaCreacion"));

        return productoRepository.findAll(filtros, limiteOrdenado)
                .stream()
                .map(productoMapper::toResponseDTO)
                .toList();
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
