# Estructura del backend — Bubbles & Essence

Spring Boot organizado **por módulo de negocio** (no por capa técnica
plana): cada módulo es autocontenido, con sus propias capas adentro.

```mermaid
flowchart TB
    subgraph common["common/ (transversal)"]
        Exc["exception/\nGlobalExceptionHandler"]
        Resp["response/\nApiResponse&lt;T&gt;"]
        Conf["config/\nSecurityConfig · CorsConfig\nOpenApiConfig"]
    end

    subgraph seguridad["seguridad/"]
        Usuario["usuario/\nUsuario (staff)"]
        Auth["auth/\nJWT · ClientePrincipal\nUsuarioDetailsService"]
        Modulo["modulo/"]
        Accion["accion/"]
        Grupo["grupo/"]
        Permiso["permiso/\n(permisos directos por usuario)"]
        Acceso["acceso/\nGET /accesos/mis-accesos"]
    end

    subgraph maestros["maestros/"]
        Ingrediente["ingrediente/"]
        Cliente["cliente/"]
    end

    subgraph ventas["ventas/"]
        Producto["producto/"]
        ProdIng["productoingrediente/\n(receta)"]
        Pedido["pedido/\nPedidoDetalle · EstadoPedido\nMotivoCancelacion"]
    end

    Pedido -.FK.-> Producto
    Pedido -.FK.-> Cliente
    ProdIng -.FK.-> Producto
    ProdIng -.FK.-> Ingrediente
    Acceso -.lee.-> Grupo
    Acceso -.lee.-> Permiso
    Grupo -.contiene.-> Accion
    Accion -.pertenece a.-> Modulo
```

## Patrón de capas dentro de cada módulo

Todos los módulos (`producto`, `ingrediente`, `pedido`, `usuario`...)
siguen la misma estructura interna:

```mermaid
flowchart LR
    Client["Cliente HTTP\n(Postman / Angular)"]
    Controller["Controller\n@RestController\nvalida DTO de entrada"]
    Service["Service / ServiceImpl\nlógica de negocio"]
    Mapper["Mapper\nEntity ⇄ DTO"]
    Repository["Repository\nJpaRepository"]
    Entity["Entity\n@Entity, mapea a tabla real"]
    DB[("PostgreSQL")]

    Client -- "JSON" --> Controller
    Controller --> Service
    Service --> Mapper
    Service --> Repository
    Repository --> Entity
    Entity --> DB
    Mapper -. "Response DTO\n(nunca expone la Entity)" .-> Client
```

Ejemplo real: `ventas/producto/` = `Producto.java` (entity) +
`ProductoRepository.java` + `ProductoService.java` (interfaz) +
`ProductoServiceImpl.java` + `ProductoMapper.java` +
`ProductoController.java` + `dto/ProductoRequestDTO.java` +
`dto/ProductoResponseDTO.java`.

## Matriz de autorización por endpoint (`@PreAuthorize` real, por módulo)

| Módulo / acción | ADMIN | OPERADOR | VENDEDOR | REPARTIDOR | CLIENTE | Invitado |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| **Usuarios** (CRUD) | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Módulos / Acciones / Grupos** (CRUD) | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Permisos directos por usuario** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Ingredientes** (crear/editar/desactivar) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Clientes** (ver/gestionar, admin) | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Productos** (crear/editar/desactivar) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Receta producto-ingrediente** (CRUD) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Pedidos**: listar / ver detalle | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Pedidos**: confirmar/rechazar pago, cambiar estado, cancelar (admin) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Pedidos**: crear pedido | — | — | — | — | ✅ | ✅ |
| **Pedidos**: ver "mis pedidos" | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| **Pedidos**: seguimiento por código + cancelar (invitado) | — | — | — | — | — | ✅ |

> **Nota**: el rol `REPARTIDOR` existe en el enum `RolUsuario` pero **no
> tiene ningún endpoint asignado todavía** — queda pendiente de diseñar
> cuando se construya el flujo de entregas (ver sección "Qué falta" del
> README del backend).

## Seguridad dual: dos tipos de token, un solo filtro

```mermaid
flowchart LR
    Req["Request entrante"] --> Filter["JwtAuthenticationFilter"]
    Filter --> Check{"¿Header\nX-API-KEY?"}
    Check -- "sí, coincide" --> SysAuth["Authentication\nROLE_SYSTEM"]
    Check -- "no" --> JwtCheck{"JWT válido?"}
    JwtCheck -- "tipo=USUARIO" --> StaffAuth["UsuarioPrincipal\n(ADMIN/OPERADOR/VENDEDOR/REPARTIDOR)"]
    JwtCheck -- "tipo=CLIENTE" --> ClienteAuth["ClientePrincipal\n(rol CLIENTE)"]
    JwtCheck -- "inválido/ausente" --> Anon["Anónimo\n(solo pasa si el endpoint es público)"]
```
