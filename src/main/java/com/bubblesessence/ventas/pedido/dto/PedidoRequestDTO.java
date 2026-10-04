package com.bubblesessence.ventas.pedido.dto;

import com.bubblesessence.ventas.pedido.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Para un cliente CON cuenta: envía clienteId.
 * Para un INVITADO (sin cuenta, el caso normal): envía invitadoNombre y
 * invitadoTelefono, deja clienteId en null. El teléfono queda guardado como
 * "llave" para que después pueda hacer seguimiento del pedido sin loguearse.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequestDTO {

    private Long clienteId;

    private String invitadoNombre;

    private String invitadoTelefono;

    @NotNull(message = "El tipo de entrega es obligatorio")
    private TipoEntrega tipoEntrega;

    private String direccionEntrega;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    @Valid
    private List<PedidoItemRequestDTO> items;

    @AssertTrue(message = "Debes enviar clienteId (cliente con cuenta) o invitadoNombre + invitadoTelefono")
    public boolean isClienteOInvitadoValido() {
        boolean tieneCliente = clienteId != null;
        boolean tieneInvitado = invitadoNombre != null && !invitadoNombre.isBlank()
                && invitadoTelefono != null && !invitadoTelefono.isBlank();
        return tieneCliente ^ tieneInvitado; // exactamente uno de los dos, nunca ambos ni ninguno
    }

    @AssertTrue(message = "direccionEntrega es obligatoria cuando tipoEntrega es DELIVERY")
    public boolean isDireccionValidaSiDelivery() {
        return tipoEntrega != TipoEntrega.DELIVERY || (direccionEntrega != null && !direccionEntrega.isBlank());
    }
}
