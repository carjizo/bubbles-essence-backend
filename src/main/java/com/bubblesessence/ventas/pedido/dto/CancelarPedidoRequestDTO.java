package com.bubblesessence.ventas.pedido.dto;

import com.bubblesessence.ventas.pedido.MotivoCancelacion;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Usado tanto por staff (PATCH /pedidos/{id}/cancelar) como, sin motivo
 *  forzado, por el propio flujo público de seguimiento (ver PedidoController). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelarPedidoRequestDTO {

    @NotNull(message = "El motivo de cancelación es obligatorio")
    private MotivoCancelacion motivoCancelacion;
}
