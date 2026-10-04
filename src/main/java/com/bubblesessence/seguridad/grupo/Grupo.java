package com.bubblesessence.seguridad.grupo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Grupo de usuarios (ej. ADMINISTRADORES, VENTAS) al que se le asignan
 * acciones. Mapea grp_seg.tbl_seg_grupo
 */
@Entity
@Table(name = "tbl_seg_grupo", schema = "grp_seg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_grupo")
    private Integer id;

    @Column(name = "cpccodigo", length = 20, nullable = false)
    private String codigo;

    @Column(name = "cpcnombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "cpcdescripcion", length = 200)
    private String descripcion;

    @Column(name = "cpbactivo", nullable = false)
    private Boolean activo;

    @Column(name = "cpdfechacreacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (activo == null) {
            activo = true;
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
