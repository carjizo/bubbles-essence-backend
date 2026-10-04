package com.bubblesessence.ventas.pedido;

import com.bubblesessence.ventas.pedido.dto.*;

import java.util.List;

public interface PedidoService {

    /** Crea el pedido. vendedorId es null cuando lo crea un invitado directo. */
    PedidoResponseDTO crear(PedidoRequestDTO request, Long vendedorId);

    List<PedidoResponseDTO> listar(EstadoPedido estado);

    PedidoResponseDTO obtenerPorId(Long id);

    /** Seguimiento público: exige que el documento coincida con el dueño del pedido. */
    PedidoResponseDTO obtenerPublicoPorCodigoYDocumento(String codigoPedido, String documento);

    /** Búsqueda para admin: obtener pedido por código SIN validar teléfono. */
    PedidoResponseDTO obtenerPorCodigoParaAdmin(String codigoPedido);

    /** Obtener todos los pedidos de un cliente logueado */
    List<PedidoResponseDTO> obtenerPorCliente(Long clienteId);

    PedidoResponseDTO cambiarEstado(Long id, CambiarEstadoPedidoRequestDTO request);

    PedidoResponseDTO cancelar(Long id, CancelarPedidoRequestDTO request, CanceladoPor canceladoPor);

    /** Cancelación pública: el invitado se identifica con código + documento, no con id. */
    PedidoResponseDTO cancelarPublico(String codigoPedido, String documento);

    /**
     * OPCIÓN A - RÁPIDO: Confirmar pago de un pedido (PENDING_PAYMENT → PAID).
     * El trabajador verifica el comprobante en Yape y confirma.
     * Se consume el stock en este momento.
     */
    PedidoResponseDTO confirmarPago(Long pedidoId, ConfirmarPagoPedidoRequestDTO request, Long usuarioId);

    /**
     * OPCIÓN A - RÁPIDO: Rechazar pago de un pedido.
     * El trabajador detecta comprobante falso o monto no coincide.
     * El pedido sigue en PENDING_PAYMENT, permitiendo reintentar.
     */
    PedidoResponseDTO rechazarPago(Long pedidoId, RechazarPagoPedidoRequestDTO request, Long usuarioId);
}
