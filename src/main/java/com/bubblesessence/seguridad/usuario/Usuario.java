package com.bubblesessence.seguridad.usuario;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Mapea grp_seg.tbl_seg_usuario (personal interno: admin, operador,
 * vendedor, repartidor). Los nombres de columna quedan explícitos con
 * @Column porque en Postgres, al no ir entre comillas en el script,
 * los identificadores como "cpnID_Usuario" se guardan en minúsculas
 * ("cpnid_usuario"), sin separar camelCase con guiones bajos.
 */
@Entity
@Table(
        name = "tbl_seg_usuario",
        schema = "grp_seg",
        uniqueConstraints = {
                @UniqueConstraint(name = "ak_usuario_login", columnNames = "cpcusuario"),
                @UniqueConstraint(name = "ak_usuario_correo", columnNames = "cpccorreo")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpnid_usuario")
    private Long id;

    @Column(name = "cpcnombrecompleto", length = 100, nullable = false)
    private String nombreCompleto;

    @Column(name = "cpcusuario", length = 30, nullable = false)
    private String usuario;

    @Column(name = "cpcclavehash", length = 255, nullable = false)
    private String claveHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "cpcrol", length = 20)
    @Builder.Default
    private RolUsuario rol = RolUsuario.VENDEDOR;

    @Column(name = "cpctelefono", length = 20)
    private String telefono;

    @Column(name = "cpccorreo", length = 100)
    private String correo;

    @Column(name = "cpbactivo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "cpdfechacreacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "cpcusuariocreador", length = 30, updatable = false)
    private String usuarioCreador;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (usuarioCreador == null) {
            usuarioCreador = "SISTEMA";
        }
        if (activo == null) {
            activo = true;
        }
    }
}
