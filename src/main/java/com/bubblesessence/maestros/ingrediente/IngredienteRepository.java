package com.bubblesessence.maestros.ingrediente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Ingrediente> findByActivo(Boolean activo);
}
