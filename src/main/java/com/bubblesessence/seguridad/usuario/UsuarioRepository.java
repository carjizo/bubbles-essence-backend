package com.bubblesessence.seguridad.usuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuario(String usuario);

    boolean existsByCorreo(String correo);

    List<Usuario> findByActivo(Boolean activo);

    List<Usuario> findByRol(RolUsuario rol);
}
