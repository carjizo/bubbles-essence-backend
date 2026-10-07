package com.bubblesessence.ventas.pedido.dto;

import com.bubblesessence.ventas.pedido.CanceladoPor;
import com.bubblesessence.ventas.pedido.EstadoPedido;
import com.bubblesessence.ventas.pedido.MotivoCancelacion;
import com.bubblesessence.ventas.pedido.TipoEntrega;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    private Long id;
    private String codigoPedido;
    private Long clienteId;
    private String invitadoNombre;
    private String invitadoTelefono;
    private String invitadoDocumento;
    private String invitadoCorreo;
    private TipoEntrega tipoEntrega;
    private String direccionEntrega;
    private EstadoPedido estadoPedido;
    private Long vendedorId;
    private BigDecimal montoTotal;
    private CanceladoPor canceladoPor;
    private MotivoCancelacion motivoCancelacion;
    private LocalDateTime fechaCancelacion;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaActualizacion;
    private List<PedidoDetalleResponseDTO> detalles;
}