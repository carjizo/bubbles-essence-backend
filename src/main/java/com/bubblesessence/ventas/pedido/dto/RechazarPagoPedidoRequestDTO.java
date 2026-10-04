package com.bubblesessence.ventas.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para rechazar pago de un pedido (PENDING_PAYMENT → mantiene PENDING o rechaza).
 * El trabajador detecta que el comprobante es falso o el monto no coincide.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechazarPagoPedidoRequestDTO {

    /**
     * Motivo del rechazo (ej: "Comprobante no válido", "Monto no coincide", "Número no encontrado")
     */
    @NotBlank(message = "El motivo del rechazo es requerido")
    private String motivo;

    /**
     * Observación adicional para el cliente (se puede enviar por WhatsApp)
     */
    private String observacion;

    /**
     * ID del usuario que rechaza (trabajador/operador)
     */
    private Long rechazadoPor;
}
