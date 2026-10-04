package com.bubblesessence.seguridad.modulo;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de módulos/pantallas del sistema (para el menú del front).
 * Mapea grp_seg.tbl_seg_modulo
 */
@Entity
@Table(name = "tbl_seg_modulo", schema = "grp_seg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_modulo")
    private Integer id;

    @Column(name = "cpccodigo", length = 20, nullable = false)
    private String codigo;

    @Column(name = "cpcnombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "cpcdescripcion", length = 200)
    private String descripcion;

    @Column(name = "cpnorden")
    private Integer orden;

    @Column(name = "cpbactivo", nullable = false)
    private Boolean activo;

    @PrePersist
    public void prePersist() {
        if (activo == null) {
            activo = true;
        }
        if (orden == null) {
            orden = 0;
        }
    }
}
