package com.bubblesessence.ventas.pedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByCodigoPedido(String codigoPedido);

    boolean existsByCodigoPedido(String codigoPedido);

    @Query("SELECT p FROM Pedido p WHERE p.invitadoDocumento = :documento AND p.cliente IS NULL")
    List<Pedido> findByInvitadoDocumentoAndClienteIsNull(@Param("documento") String documento);

    List<Pedido> findTop50ByOrderByFechaPedidoDesc();

    List<Pedido> findTop50ByEstadoPedidoOrderByFechaPedidoDesc(EstadoPedido estado);
}
