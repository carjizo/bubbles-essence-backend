package com.bubblesessence.maestros.cliente;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Clientes CON cuenta (historial, "mis pedidos"). El invitado sin cuenta
 * NO pasa por acá: sus datos van directo en tbl_ven_pedido
 * (cpcinvitadonombre / cpcinvitadotelefono). Mapea grp_mge.tbl_mge_cliente
 */
@Entity
@Table(
        name = "tbl_mge_cliente",
        schema = "grp_mge",
        uniqueConstraints = {
            @UniqueConstraint(name = "ak_cliente_documento", columnNames = "cpcdocumento"),
            @UniqueConstraint(name = "ak_cliente_correo", columnNames = "cpccorreo")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_cliente")
    private Long id;

    @Column(name = "cpcdocumento", length = 20, nullable = false)
    private String documento;

    @Column(name = "cpcnombrecompleto", length = 100, nullable = false)
    private String nombreCompleto;

    @Column(name = "cpccorreo", length = 100, nullable = false)
    private String correo;

    @Column(name = "cpcclavehash", length = 255, nullable = false)
    private String claveHash;

    @Column(name = "cpctelefono", length = 20)
    private String telefono;

    @Column(name = "cpcdireccion", length = 255)
    private String direccion;

    @Column(name = "cpcinstagram", length = 50)
    private String instagram;

    @Column(name = "cpbactivo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "cpdfecharegistro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}
