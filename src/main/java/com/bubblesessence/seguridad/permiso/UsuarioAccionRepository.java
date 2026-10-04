package com.bubblesessence.seguridad.permiso;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioAccionRepository extends JpaRepository<UsuarioAccion, Long> {

    List<UsuarioAccion> findByUsuario_Id(Long usuarioId);

    Optional<UsuarioAccion> findByUsuario_IdAndAccion_Id(Long usuarioId, Integer accionId);

    boolean existsByUsuario_IdAndAccion_Id(Long usuarioId, Integer accionId);
}
