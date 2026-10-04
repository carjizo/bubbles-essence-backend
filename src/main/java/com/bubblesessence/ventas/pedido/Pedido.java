package com.bubblesessence.ventas.pedido;

import com.bubblesessence.maestros.cliente.Cliente;
import com.bubblesessence.seguridad.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PEDIDO. Puede pertenecer a un Cliente con cuenta (cpnid_cliente) o a un
 * invitado sin cuenta (cpcinvitadonombre/telefono); nunca ambos a la vez
 * (ver validación en PedidoRequestDTO). Mapea grp_ven.tbl_ven_pedido
 */
@Entity
@Table(
        name = "tbl_ven_pedido",
        schema = "grp_ven",
        uniqueConstraints = @UniqueConstraint(name = "ak_pedido_codigo", columnNames = "cpccodigopedido")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_pedido")
    private Long id;

    @Column(name = "cpccodigopedido", length = 20, nullable = false)
    private String codigoPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_cliente")
    private Cliente cliente;

    @Column(name = "cpcinvitadonombre", length = 100)
    private String invitadoNombre;

    @Column(name = "cpcinvitadotelefono", length = 20)
    private String invitadoTelefono;

    @Column(name = "cpcinvitadodocumento", length = 20)
    private String invitadoDocumento;

    @Column(name = "cpcinvitadocorreo", length = 100)
    private String invitadoCorreo;

    @Enumerated(EnumType.STRING)
    @Column(name = "cpctipoentrega", length = 20, nullable = false)
    private TipoEntrega tipoEntrega;

    @Column(name = "cpcdireccionentrega", length = 255)
    private String direccionEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "cpcestadopedido", length = 20, nullable = false)
    private EstadoPedido estadoPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_vendedor")
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_repartidor")
    private Usuario repartidor;

    @Column(name = "cpnmontototal", nullable = false)
    private BigDecimal montoTotal;

    @Column(name = "cpcreferenciayape", length = 50)
    private String referenciaYape;

    @Column(name = "cpbstock_reservado")
    @Builder.Default
    private Boolean stockReservado = true;  // true = reservado (no consumido), false = consumido

    @Column(name = "cpdfechaexpiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "cpdfechaentregaesperada")
    private LocalDateTime fechaEntregaEsperada;

    @Enumerated(EnumType.STRING)
    @Column(name = "cpccanceladopor", length = 20)
    private CanceladoPor canceladoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "cpcmotivocancelacion", length = 30)
    private MotivoCancelacion motivoCancelacion;

    @Column(name = "cpdfechacancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "cpdfechapedido", nullable = false, updatable = false)
    private LocalDateTime fechaPedido;

    @Column(name = "cpdfechaactualizacion")
    private LocalDateTime fechaActualizacion;

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaPedido == null) {
            fechaPedido = LocalDateTime.now();
        }
        if (estadoPedido == null) {
            estadoPedido = EstadoPedido.PENDING_PAYMENT;
        }
    }

    public void agregarDetalle(PedidoDetalle detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }

    /** El teléfono "dueño" del pedido, sea cliente con cuenta o invitado. Se usa para validar el seguimiento público. */
    public String getTelefonoContacto() {
        return cliente != null ? cliente.getTelefono() : invitadoTelefono;
    }

    /** El documento "dueño" del pedido, sea cliente con cuenta o invitado. Se usa para validar el seguimiento público. */
    public String getDocumentoContacto() {
        return cliente != null ? cliente.getDocumento() : invitadoDocumento;
    }
}
