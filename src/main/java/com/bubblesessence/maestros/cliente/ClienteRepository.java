package com.bubblesessence.maestros.cliente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByTelefono(String telefono);

    boolean existsByTelefono(String telefono);

    Optional<Cliente> findByDocumento(String documento);

    boolean existsByDocumento(String documento);

    Optional<Cliente> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
