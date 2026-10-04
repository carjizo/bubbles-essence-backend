package com.bubblesessence.ventas.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para confirmar pago de un pedido (PENDING_PAYMENT → PAID).
 * El trabajador verifica el comprobante en Yape y confirma el pago.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmarPagoPedidoRequestDTO {

    /**
     * Número de operación/referencia en Yape (ej: "12345678")
     */
    @NotBlank(message = "La referencia Yape es requerida")
    private String referenciaYape;

    /**
     * Monto verificado (debe coincidir con el total del pedido).
     * Si no coincide, se rechaza la confirmación.
     */
    @NotNull(message = "El monto verificado es requerido")
    private BigDecimal montoVerificado;

    /**
     * Observación/nota del trabajador (ej: "Dinero recibido", "Comprobante OK")
     */
    private String observacion;

    /**
     * ID del usuario que está verificando el pago (trabajador/operador)
     * Puede venir del JWT o ser opcional si lo obtiene del contexto
     */
    private Long verificadoPor;
}
