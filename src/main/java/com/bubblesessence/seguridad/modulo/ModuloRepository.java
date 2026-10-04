package com.bubblesessence.seguridad.modulo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

    Optional<Modulo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Modulo> findAllByOrderByOrdenAsc();
}
