package com.bubblesessence.seguridad.grupo;

import com.bubblesessence.seguridad.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** A qué grupos pertenece un usuario (N:M). Mapea grp_seg.tbl_seg_usuariogrupo */
@Entity
@Table(name = "tbl_seg_usuariogrupo", schema = "grp_seg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_usuariogrupo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_grupo", nullable = false)
    private Grupo grupo;

    @Column(name = "cpdfechaasignacion", nullable = false, updatable = false)
    private LocalDateTime fechaAsignacion;

    @PrePersist
    public void prePersist() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }
    }
}
