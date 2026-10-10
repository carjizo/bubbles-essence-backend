package com.bubblesessence.ventas.pedido;

import com.bubblesessence.common.exception.BusinessException;
import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.maestros.cliente.Cliente;
import com.bubblesessence.maestros.cliente.ClienteRepository;
import com.bubblesessence.seguridad.usuario.Usuario;
import com.bubblesessence.seguridad.usuario.UsuarioRepository;
import com.bubblesessence.ventas.pedido.dto.*;
import com.bubblesessence.ventas.producto.Producto;
import com.bubblesessence.ventas.producto.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    /** Estados desde los que ya NO se puede cancelar (ni staff ni invitado). */
    private static final Set<EstadoPedido> NO_CANCELABLES = Set.of(
            EstadoPedido.SHIPPED, EstadoPedido.DELIVERED, EstadoPedido.CANCELLED);

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoMapper pedidoMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO request, Long vendedorId) {
        Cliente cliente = null;
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));
        }

        Usuario vendedor = null;
        if (vendedorId != null) {
            vendedor = usuarioRepository.findById(vendedorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", vendedorId));
        }

        // Primero genera un código temporal (será reemplazado después)
        Long proximoId = pedidoRepository.count() + 1;
        String codigoTemporal = generarCodigo(proximoId);

        Pedido pedido = Pedido.builder()
                .codigoPedido(codigoTemporal)
                .cliente(cliente)
                .invitadoNombre(request.getInvitadoNombre())
                .invitadoTelefono(request.getInvitadoTelefono())
                .invitadoDocumento(request.getInvitadoDocumento())
                .invitadoCorreo(request.getInvitadoCorreo())
                .tipoEntrega(request.getTipoEntrega())
                .direccionEntrega(request.getDireccionEntrega())
                .vendedor(vendedor)
                .estadoPedido(EstadoPedido.PENDING_PAYMENT)
                .montoTotal(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (PedidoItemRequestDTO item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", item.getProductoId()));
            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new BusinessException("El producto '%s' no está disponible".formatted(producto.getNombre()));
            }

            // Validar stock disponible
            if (producto.getStock() == null || producto.getStock() < item.getCantidad()) {
                int disponible = producto.getStock() != null ? producto.getStock() : 0;
                throw new BusinessException(
                        "Stock insuficiente de '%s'. Disponible: %d, Solicitado: %d"
                                .formatted(producto.getNombre(), disponible, item.getCantidad())
                );
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            PedidoDetalle detalle = PedidoDetalle.builder()
                    .producto(producto)
                    .cantidad(item.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .build();
            pedido.agregarDetalle(detalle);
            total = total.add(subtotal);
        }
        pedido.setMontoTotal(total);

        Pedido guardado = pedidoRepository.save(pedido);
        // Reemplaza el código con el real basado en el ID autogenerado
        guardado.setCodigoPedido(generarCodigo(guardado.getId()));
        guardado = pedidoRepository.save(guardado);

        return pedidoMapper.toResponseDTO(guardado);
    }

    /** Tope de pedidos por consulta; el front pagina esas filas localmente. */
    private static final int LIMITE_LISTADO = 100;

    @Override
    public List<PedidoResponseDTO> listar(EstadoPedido estado) {
        // Más recientes primero. El id desempata pedidos con la misma fecha para
        // que el orden sea estable entre consultas (si no, dos recargas seguidas
        // podrían devolver esos pedidos intercambiados).
        Pageable ultimos = PageRequest.of(0, LIMITE_LISTADO,
                Sort.by(Sort.Order.desc("fechaPedido"), Sort.Order.desc("id")));

        List<Pedido> pedidos = (estado != null)
                ? pedidoRepository.findByEstadoPedido(estado, ultimos)
                : pedidoRepository.findAllBy(ultimos);

        return pedidos.stream().map(pedidoMapper::toResponseDTO).toList();
    }

    @Override
    public PedidoResponseDTO obtenerPorId(Long id) {
        return pedidoMapper.toResponseDTO(buscarEntidadOFallar(id));
    }

    @Override
    public PedidoResponseDTO obtenerPublicoPorCodigoYDocumento(String codigoPedido, String documento) {
        return pedidoMapper.toResponseDTO(buscarPublicoOFallar(codigoPedido, documento));
    }

    @Override
    public PedidoResponseDTO obtenerPorCodigoParaAdmin(String codigoPedido) {
        return pedidoMapper.toResponseDTO(buscarEntidadPorCodigoOFallar(codigoPedido));
    }

    @Override
    public List<PedidoResponseDTO> obtenerPorCliente(Long clienteId) {
        return pedidoRepository.findAll().stream()
                .filter(p -> p.getCliente() != null && p.getCliente().getId().equals(clienteId))
                .map(pedidoMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PedidoResponseDTO cambiarEstado(Long id, CambiarEstadoPedidoRequestDTO request) {
        Pedido pedido = buscarEntidadOFallar(id);
        EstadoPedido estadoActual = pedido.getEstadoPedido();
        EstadoPedido nuevoEstado = request.getNuevoEstado();

        // OPCIÓN B - COMPLETO: Validar transiciones permitidas
        validarTransicionEstado(estadoActual, nuevoEstado, pedido.getTipoEntrega());

        // Si está cancelado, no se puede cambiar
        if (estadoActual == EstadoPedido.CANCELLED) {
            throw new BusinessException("No se puede cambiar el estado de un pedido cancelado");
        }

        pedido.setEstadoPedido(nuevoEstado);
        pedido.setFechaActualizacion(LocalDateTime.now());
        return pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    /**
     * OPCIÓN B - COMPLETO: Valida transiciones permitidas entre estados.
     * Define la máquina de estados permitida.
     */
    private void validarTransicionEstado(EstadoPedido actual, EstadoPedido nuevo, TipoEntrega tipoEntrega) {
        // Pedidos entregados y cancelados no se pueden cambiar
        if (actual == EstadoPedido.DELIVERED || actual == EstadoPedido.CANCELLED) {
            throw new BusinessException("Este pedido no puede cambiar de estado");
        }

        // Mapa de transiciones permitidas
        boolean transicionValida = switch (actual) {
            case PENDING_PAYMENT -> nuevo == EstadoPedido.PAID;
            case PAID -> nuevo == EstadoPedido.PREPARING;
            case PREPARING -> nuevo == EstadoPedido.READY_TO_SHIP;
            case READY_TO_SHIP -> {
                if (tipoEntrega == TipoEntrega.DELIVERY) {
                    yield nuevo == EstadoPedido.SHIPPED;
                } else {
                    yield nuevo == EstadoPedido.DELIVERED;
                }
            }
            case SHIPPED -> nuevo == EstadoPedido.DELIVERED;
            default -> false;
        };

        if (!transicionValida) {
            throw new BusinessException(
                    "Transición no permitida: " + actual + " → " + nuevo
            );
        }
    }

    @Override
    @Transactional
    public PedidoResponseDTO cancelar(Long id, CancelarPedidoRequestDTO request, CanceladoPor canceladoPor) {
        Pedido pedido = buscarEntidadOFallar(id);
        aplicarCancelacion(pedido, canceladoPor, request.getMotivoCancelacion());
        return pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoResponseDTO cancelarPublico(String codigoPedido, String documento) {
        Pedido pedido = buscarPublicoOFallar(codigoPedido, documento);
        aplicarCancelacion(pedido, CanceladoPor.CUSTOMER, MotivoCancelacion.CUSTOMER_REQUEST);
        return pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    /**
     * OPCIÓN A - RÁPIDO: Confirmar pago de un pedido (PENDING_PAYMENT → PAID).
     * El trabajador verifica el comprobante en Yape y confirma.
     * Se consume el stock en este momento.
     */
    @Override
    @Transactional
    public PedidoResponseDTO confirmarPago(Long pedidoId, ConfirmarPagoPedidoRequestDTO request, Long usuarioId) {
        Pedido pedido = buscarEntidadOFallar(pedidoId);

        // Validar estado
        if (pedido.getEstadoPedido() != EstadoPedido.PENDING_PAYMENT) {
            throw new BusinessException(
                    "No se puede confirmar pago de un pedido en estado " + pedido.getEstadoPedido()
                            + ". Solo se puede confirmar desde PENDING_PAYMENT"
            );
        }

        // Validar monto verificado coincida con total
        if (request.getMontoVerificado().compareTo(pedido.getMontoTotal()) != 0) {
            throw new BusinessException(
                    "Monto verificado no coincide. Esperado: " + pedido.getMontoTotal()
                            + ", Verificado: " + request.getMontoVerificado()
            );
        }

        // Obtener usuario que verifica (trabajador/operador)
        Usuario verificador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        // CONSUMIR STOCK: Restar cantidad de cada producto
        for (PedidoDetalle detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto.getStock() < detalle.getCantidad()) {
                throw new BusinessException(
                        "Stock insuficiente de '%s' para confirmar pago. Disponible: %d, Necesario: %d"
                                .formatted(producto.getNombre(), producto.getStock(), detalle.getCantidad())
                );
            }
            // Consumir stock
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        // Actualizar pedido
        pedido.setEstadoPedido(EstadoPedido.PAID);
        pedido.setReferenciaYape(request.getReferenciaYape());
        pedido.setStockReservado(false);  // Ya consumido
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedido.setFechaEntregaEsperada(LocalDateTime.now().plusDays(2)); // 48h para preparación

        Pedido guardado = pedidoRepository.save(pedido);
        return pedidoMapper.toResponseDTO(guardado);
    }

    /**
     * OPCIÓN A - RÁPIDO: Rechazar pago de un pedido.
     * El trabajador detecta comprobante falso o monto no coincide.
     * El pedido sigue en PENDING_PAYMENT, permitiendo reintentar.
     * Stock sigue reservado.
     */
    @Override
    @Transactional
    public PedidoResponseDTO rechazarPago(Long pedidoId, RechazarPagoPedidoRequestDTO request, Long usuarioId) {
        Pedido pedido = buscarEntidadOFallar(pedidoId);

        // Validar estado
        if (pedido.getEstadoPedido() != EstadoPedido.PENDING_PAYMENT) {
            throw new BusinessException(
                    "No se puede rechazar pago de un pedido en estado " + pedido.getEstadoPedido()
            );
        }

        // Obtener usuario que rechaza (trabajador/operador)
        Usuario rechazador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        // NOTA: En una implementación completa, aquí se crearía una INCIDENCIA
        // Para ahora, solo registramos que fue rechazado (puede agregar campo en Pedido)
        // y mantenemos PENDING_PAYMENT para que cliente reintente

        pedido.setFechaActualizacion(LocalDateTime.now());
        // TODO: Crear incidencia con motivo de rechazo

        Pedido guardado = pedidoRepository.save(pedido);
        return pedidoMapper.toResponseDTO(guardado);
    }

    private void aplicarCancelacion(Pedido pedido, CanceladoPor canceladoPor, MotivoCancelacion motivo) {
        // OPCIÓN B - COMPLETO: Validaciones según estado
        // Estados desde los que NO se puede cancelar
        if (NO_CANCELABLES.contains(pedido.getEstadoPedido())) {
            throw new BusinessException(
                    "No se puede cancelar un pedido en estado " + pedido.getEstadoPedido());
        }

        // Validación adicional: si está en PREPARING, solo admin puede cancelar
        if (pedido.getEstadoPedido() == EstadoPedido.PREPARING && canceladoPor != CanceladoPor.ADMIN) {
            throw new BusinessException(
                    "Los pedidos en preparación solo pueden ser cancelados por ADMIN"
            );
        }

        // Si el estado es PAID o PREPARING, se debe liberar stock
        if ((pedido.getEstadoPedido() == EstadoPedido.PAID ||
                pedido.getEstadoPedido() == EstadoPedido.PREPARING) &&
                !Boolean.TRUE.equals(pedido.getStockReservado())) {
            // Stock ya fue consumido, necesita ser liberado
            for (PedidoDetalle detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        // Aplicar cancelación
        pedido.setEstadoPedido(EstadoPedido.CANCELLED);
        pedido.setCanceladoPor(canceladoPor);
        pedido.setMotivoCancelacion(motivo);
        pedido.setFechaCancelacion(LocalDateTime.now());
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedido.setStockReservado(false);
    }

    private Pedido buscarEntidadOFallar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }

    /** No usa findById a propósito: un invitado nunca debe poder "adivinar" un
     *  pedido ajeno solo por id secuencial. Necesita el código Y el documento. */
    private Pedido buscarPublicoOFallar(String codigoPedido, String documento) {
        Pedido pedido = pedidoRepository.findByCodigoPedido(codigoPedido)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún pedido con ese código"));
        if (!pedido.getDocumentoContacto().equals(documento)) {
            throw new ResourceNotFoundException("No se encontró ningún pedido con ese código");
        }
        return pedido;
    }

    /** Para admin: buscar por código sin validar teléfono */
    private Pedido buscarEntidadPorCodigoOFallar(String codigoPedido) {
        return pedidoRepository.findByCodigoPedido(codigoPedido)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún pedido con ese código"));
    }

    private String generarCodigo(Long id) {
        try {
            // Obtener el siguiente valor de la secuencia PostgreSQL
            Number secuenciaValue = (Number) entityManager.createNativeQuery(
                            "SELECT nextval('grp_ven.seq_pedido_codigo')")
                    .getSingleResult();
            Long numeroSecuencial = secuenciaValue.longValue();
            return "PED-" + Year.now().getValue() + "-" + String.format("%06d", numeroSecuencial);
        } catch (Exception e) {
            // Fallback: usar el ID del pedido si hay problema con la secuencia
            return "PED-" + Year.now().getValue() + "-" + String.format("%06d", id);
        }
    }
}
