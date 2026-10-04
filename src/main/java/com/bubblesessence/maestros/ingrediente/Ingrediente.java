package com.bubblesessence.maestros.ingrediente;

import jakarta.persistence.*;
import lombok.*;

/**
 * Mapea grp_mge.tbl_mge_ingrediente: catálogo de insumos
 * (cúrcuma, miel, café, etc.) usados en las recetas de los jabones.
 */
@Entity
@Table(
        name = "tbl_mge_ingrediente",
        schema = "grp_mge",
        uniqueConstraints = @UniqueConstraint(name = "ak_ingrediente_nombre", columnNames = "cpcnombre")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_ingrediente")
    private Integer id;

    @Column(name = "cpcnombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "cpcdescripcion", length = 200)
    private String descripcion;

    @Column(name = "cpbactivo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @PrePersist
    public void prePersist() {
        if (activo == null) {
            activo = true;
        }
    }
}
