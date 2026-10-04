package com.bubblesessence.seguridad.accion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccionRepository extends JpaRepository<Accion, Integer> {

    Optional<Accion> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Accion> findByModulo_Id(Integer moduloId);
}
