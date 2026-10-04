package com.bubblesessence.seguridad.grupo;

import com.bubblesessence.seguridad.accion.Accion;
import jakarta.persistence.*;
import lombok.*;

/** Qué acciones tiene un grupo (N:M). Mapea grp_seg.tbl_seg_grupoaccion */
@Entity
@Table(name = "tbl_seg_grupoaccion", schema = "grp_seg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoAccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_grupoaccion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_grupo", nullable = false)
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_accion", nullable = false)
    private Accion accion;
}
