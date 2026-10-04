package com.bubblesessence.ventas.pedido.dto;

import com.bubblesessence.ventas.pedido.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoPedidoRequestDTO {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoPedido nuevoEstado;
}
