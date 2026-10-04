package com.bubblesessence.seguridad.permiso;

import com.bubblesessence.seguridad.accion.Accion;
import com.bubblesessence.seguridad.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Permiso puntual (GRANT/DENY) a nivel de usuario individual. Mapea grp_seg.tbl_seg_usuarioaccion */
@Entity
@Table(name = "tbl_seg_usuarioaccion", schema = "grp_seg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioAccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_usuarioaccion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpnid_accion", nullable = false)
    private Accion accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "cpctipo", length = 10, nullable = false)
    private TipoPermiso tipo;

    @Column(name = "cpdfechaasignacion", nullable = false, updatable = false)
    private LocalDateTime fechaAsignacion;

    @PrePersist
    public void prePersist() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }
        if (tipo == null) {
            tipo = TipoPermiso.GRANT;
        }
    }
}
