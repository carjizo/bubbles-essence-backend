package com.bubblesessence.ventas.producto;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mapea grp_ven.tbl_ven_producto: catálogo de jabones a la venta.
 */
@Entity
@Table(
        name = "tbl_ven_producto",
        schema = "grp_ven",
        uniqueConstraints = {
                @UniqueConstraint(name = "ak_producto_nombre", columnNames = "cpcnombre"),
                @UniqueConstraint(name = "ak_producto_codigo", columnNames = "cpccodigo")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_producto")
    private Integer id;

    @Column(name = "cpccodigo", length = 50)
    private String codigo;

    @Column(name = "cpcnombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "cpcdescripcion", length = 200)
    private String descripcion;

    @Column(name = "cpnprecio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "cpnstock")
    private Integer stock;

    @Version
    @Column(name = "cpn_version")
    private Long version;

    @Column(name = "cpcimagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "cpbusuario_creador", length = 100)
    private String usuarioCreador;

    @Column(name = "cpbactivo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "cpdfechacreacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }
}
