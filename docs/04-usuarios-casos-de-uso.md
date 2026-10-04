# Usuarios y casos de uso — Bubbles & Essence

6 tipos de actor: 4 roles de staff (tabla `tbl_seg_usuario`), clientes
registrados, e invitados sin cuenta.

```mermaid
flowchart LR
    ADMIN(["👤 ADMIN"])
    OPERADOR(["👤 OPERADOR"])
    VENDEDOR(["👤 VENDEDOR"])
    REPARTIDOR(["👤 REPARTIDOR\n(rol existe, sin casos de uso aún)"])
    CLIENTE(["🛍️ Cliente registrado"])
    INVITADO(["🧑 Invitado (sin cuenta)"])

    subgraph staffcasos["Panel interno"]
        UC1["Gestionar usuarios\ny permisos (RBAC)"]
        UC2["Gestionar catálogo\n(ingredientes, productos, receta)"]
        UC3["Gestionar clientes"]
        UC4["Ver / listar pedidos"]
        UC5["Confirmar o rechazar pago"]
        UC6["Cambiar estado\nde un pedido"]
        UC7["Cancelar un pedido\n(como staff)"]
    end

    subgraph tiendacasos["Tienda"]
        UC8["Ver catálogo público"]
        UC9["Armar carrito"]
        UC10["Hacer checkout\n(crear pedido)"]
        UC11["Ver seguimiento\npor código"]
        UC12["Cancelar su propio\npedido (por código)"]
        UC13["Ver 'Mis Pedidos'\n(historial completo)"]
        UC14["Registrarse / loguearse\ncomo cliente"]
    end

    ADMIN --> UC1 & UC2 & UC3 & UC4 & UC5 & UC6 & UC7
    OPERADOR --> UC2 & UC3 & UC4 & UC5 & UC6 & UC7
    VENDEDOR --> UC3 & UC4

    CLIENTE --> UC8 & UC9 & UC10 & UC11 & UC12 & UC13 & UC14
    INVITADO --> UC8 & UC9 & UC10 & UC11 & UC12
```

## Diferencia clave: Invitado vs Cliente registrado

| Capacidad | Invitado | Cliente registrado |
|---|:---:|:---:|
| Ver catálogo, armar carrito, hacer checkout | ✅ | ✅ |
| Seguimiento de pedido por código + documento | ✅ | ✅ |
| Cancelar un pedido propio (vía código) | ✅ | ✅ |
| Ver historial completo ("Mis Pedidos") | ❌ | ✅ |
| Pedido queda asociado a una cuenta | ❌ | ✅ |

Ningún caso de uso de compra (ver catálogo, comprar, hacer seguimiento,
cancelar) **requiere** cuenta — la cuenta de cliente solo **suma**
historial y conveniencia, nunca es un bloqueo para comprar.

## Flujo completo de un pedido (todos los actores que tocan el estado)

```mermaid
sequenceDiagram
    actor C as Cliente/Invitado
    participant T as Tienda (frontend)
    participant BE as Backend
    actor OP as OPERADOR/ADMIN

    C->>T: Arma carrito y hace checkout
    T->>BE: POST /pedidos
    BE-->>T: Pedido PENDING_PAYMENT + código

    C->>T: Sube comprobante de pago
    OP->>BE: PATCH /pedidos/{id}/confirmar-pago
    BE-->>OP: Pedido → PAID

    OP->>BE: PATCH /pedidos/{id}/estado (PREPARING)
    OP->>BE: PATCH /pedidos/{id}/estado (READY_TO_SHIP)
    OP->>BE: PATCH /pedidos/{id}/estado (SHIPPED)
    OP->>BE: PATCH /pedidos/{id}/estado (DELIVERED)

    C->>T: Consulta /seguimiento en cualquier momento
    T->>BE: GET /pedidos/seguimiento
    BE-->>C: Estado actual del pedido
```

## Pendiente de este diagrama
Falta mapear el flujo de **REPARTIDOR** (entrega física) en cuanto el
backend defina esos endpoints — hoy el estado `SHIPPED`→`DELIVERED` lo
cambia OPERADOR/ADMIN manualmente, no hay actor de reparto todavía.
