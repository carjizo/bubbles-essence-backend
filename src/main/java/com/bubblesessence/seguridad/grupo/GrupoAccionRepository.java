package com.bubblesessence.seguridad.grupo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrupoAccionRepository extends JpaRepository<GrupoAccion, Long> {

    List<GrupoAccion> findByGrupo_Id(Integer grupoId);

    List<GrupoAccion> findByGrupo_IdIn(List<Integer> grupoIds);

    Optional<GrupoAccion> findByGrupo_IdAndAccion_Id(Integer grupoId, Integer accionId);

    boolean existsByGrupo_IdAndAccion_Id(Integer grupoId, Integer accionId);
}
