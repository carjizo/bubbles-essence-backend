package com.bubblesessence.seguridad.grupo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioGrupoRepository extends JpaRepository<UsuarioGrupo, Long> {

    List<UsuarioGrupo> findByUsuario_Id(Long usuarioId);

    List<UsuarioGrupo> findByGrupo_Id(Integer grupoId);

    Optional<UsuarioGrupo> findByUsuario_IdAndGrupo_Id(Long usuarioId, Integer grupoId);

    boolean existsByUsuario_IdAndGrupo_Id(Long usuarioId, Integer grupoId);
}
