package com.bubblesessence.seguridad.grupo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoRepository extends JpaRepository<Grupo, Integer> {

    boolean existsByCodigo(String codigo);
}
