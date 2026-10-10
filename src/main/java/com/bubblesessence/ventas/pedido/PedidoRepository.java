package com.bubblesessence.ventas.pedido;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByCodigoPedido(String codigoPedido);

    boolean existsByCodigoPedido(String codigoPedido);

    @Query("SELECT p FROM Pedido p WHERE p.invitadoDocumento = :documento AND p.cliente IS NULL")
    List<Pedido> findByInvitadoDocumentoAndClienteIsNull(@Param("documento") String documento);

    /**
     * Listado interno: pedidos de UN estado, con el tope y el orden que
     * decide el Pageable (ver PedidoServiceImpl.listar). Devolver List (no
     * Page) evita la consulta COUNT extra, que acá no hace falta.
     */
    List<Pedido> findByEstadoPedido(EstadoPedido estado, Pageable pageable);

    /** Igual, pero de todos los estados. */
    List<Pedido> findAllBy(Pageable pageable);
}
