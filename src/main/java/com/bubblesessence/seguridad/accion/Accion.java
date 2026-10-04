package com.bubblesessence.seguridad.accion;

import com.bubblesessence.seguridad.modulo.Modulo;
import jakarta.persistence.*;
import lombok.*;

/**
 * Acción/botón dentro de un módulo. El campo "codigo" (ej.
 * 'btn-editar-pedido') es el valor literal que el front (Angular) usa para
 * decidir si muestra o no un botón/acción en pantalla.
 * Mapea grp_seg.tbl_seg_accion
 */
@Entity
@Table(name = "tbl_seg_accion", schema = "grp_seg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_accion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_modulo", nullable = false)
    private Modulo modulo;

    /** ej. 'btn-editar-pedido'. Es lo que consume el front, cuídalo como un identificador estable. */
    @Column(name = "cpccodigo", length = 60, nullable = false)
    private String codigo;

    @Column(name = "cpcnombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "cpcdescripcion", length = 200)
    private String descripcion;

    @Column(name = "cpbactivo", nullable = false)
    private Boolean activo;

    @PrePersist
    public void prePersist() {
        if (activo == null) {
            activo = true;
        }
    }
}
