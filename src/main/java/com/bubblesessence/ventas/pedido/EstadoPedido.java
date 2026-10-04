package com.bubblesessence.ventas.pedido;

/**
 * PENDING_PAYMENT -> PAID -> PREPARING -> READY_TO_SHIP -> SHIPPED -> DELIVERED
 * o CANCELLED en cualquier punto permitido.
 */
public enum EstadoPedido {
    PENDING_PAYMENT,
    PAID,
    PREPARING,
    READY_TO_SHIP,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
