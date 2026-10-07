package com.bubblesessence.ventas.pedido;

import com.bubblesessence.ventas.pedido.dto.PedidoDetalleResponseDTO;
import com.bubblesessence.ventas.pedido.dto.PedidoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class PedidoMapper {

    public PedidoResponseDTO toResponseDTO(Pedido pedido) {
        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .codigoPedido(pedido.getCodigoPedido())
                .clienteId(pedido.getCliente() != null ? pedido.getCliente().getId() : null)
                .invitadoNombre(pedido.getInvitadoNombre())
                .invitadoTelefono(pedido.getInvitadoTelefono())
                .invitadoDocumento(pedido.getInvitadoDocumento())
                .invitadoCorreo(pedido.getInvitadoCorreo())
                .tipoEntrega(pedido.getTipoEntrega())
                .direccionEntrega(pedido.getDireccionEntrega())
                .estadoPedido(pedido.getEstadoPedido())
                .vendedorId(pedido.getVendedor() != null ? pedido.getVendedor().getId() : null)
                .montoTotal(pedido.getMontoTotal())
                .canceladoPor(pedido.getCanceladoPor())
                .motivoCancelacion(pedido.getMotivoCancelacion())
                .fechaCancelacion(pedido.getFechaCancelacion())
                .fechaPedido(pedido.getFechaPedido())
                .fechaActualizacion(pedido.getFechaActualizacion())
                .detalles(pedido.getDetalles().stream().map(this::toDetalleDTO).toList())
                .build();
    }

    private PedidoDetalleResponseDTO toDetalleDTO(PedidoDetalle detalle) {
        return PedidoDetalleResponseDTO.builder()
                .id(detalle.getId())
                .productoId(detalle.getProducto().getId())
                .productoNombre(detalle.getProducto().getNombre())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .build();
    }
}