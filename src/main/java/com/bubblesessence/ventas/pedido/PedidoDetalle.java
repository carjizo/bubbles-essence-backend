package com.bubblesessence.ventas.pedido;

import com.bubblesessence.ventas.producto.Producto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Detalle del pedido: qué jabones y cuántos. Mapea grp_ven.tbl_ven_pedidodetalle */
@Entity
@Table(name = "tbl_ven_pedidodetalle", schema = "grp_ven")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_pedidodetalle")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_producto", nullable = false)
    private Producto producto;

    @Column(name = "cpncantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "cpnpreciounitario", nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "cpnsubtotal", nullable = false)
    private BigDecimal subtotal;
}
