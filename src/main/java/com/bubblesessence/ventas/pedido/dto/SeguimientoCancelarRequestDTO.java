package com.bubblesessence.ventas.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Para que un invitado cancele SU pedido sin loguearse: se valida que
 *  codigoPedido + documento coincidan con el dueño real del pedido. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoCancelarRequestDTO {

    @NotBlank(message = "El código de pedido es obligatorio")
    private String codigoPedido;

    @NotBlank(message = "El documento (DNI/Cédula) es obligatorio")
    private String documento;
}
