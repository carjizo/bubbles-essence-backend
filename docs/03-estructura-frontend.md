# Estructura del frontend — Bubbles & Essence

Angular con dos zonas separadas (panel interno de staff y tienda de
cliente), compartiendo la infraestructura de `core/`.

```mermaid
flowchart TB
    subgraph core["core/ (infraestructura compartida)"]
        Models["models/\nespejo 1:1 de los DTOs del backend"]
        Services["services/\nun servicio por recurso"]
        Interceptor["interceptors/\nauth.interceptor.ts\ncliente-request.context.ts"]
        Guards["guards/\nauthGuard (staff)"]
        Directives["directives/\n*appHasAccion"]
    end

    subgraph staff["features/ — Panel interno (staff)"]
        Login["auth/login/"]
        Shell["layout/shell/\nsidebar dinámico según módulos"]
        Productos["productos/\nlist + form"]
        Admin["admin/seguimiento-interno/\ngestión de pedidos"]
        Seg["seguridad/\nacciones · grupos · usuarios-permisos"]
    end

    subgraph tienda["features/cliente/ — Tienda (comprador)"]
        Catalogo["catalogo-publico/"]
        Detalle["producto-detalle/"]
        Carrito["carrito-cliente/"]
        Checkout["checkout-cliente/"]
        Seguimiento["seguimiento-pedido/\npúblico, sin login"]
        ClienteAuth["cliente-login/\ncliente-registro/"]
        MisPedidos["mis-pedidos/\nrequiere sesión CLIENTE"]
    end

    Interceptor -. "lee" .-> Services
    staff -. "usa" .-> core
    tienda -. "usa" .-> core
```

## Las dos sesiones conviviendo en el mismo navegador

```mermaid
flowchart TB
    LS[("localStorage")]
    SessStaff["be_session\n(token tipo USUARIO)"]
    SessCliente["be_session_cliente\n(token tipo CLIENTE)"]
    LS --> SessStaff
    LS --> SessCliente

    Req["Angular hace un HttpRequest"] --> Mark{"¿El servicio marcó\nUSA_TOKEN_CLIENTE\nen el HttpContext?"}
    Mark -- "sí" --> UseCliente["Usa be_session_cliente"]
    Mark -- "no (default)" --> UseStaff["Usa be_session"]

    style Mark fill:#2d2d6b
```

**Por qué por `HttpContext` y no por URL**: varios endpoints de cliente
comparten la misma ruta base que endpoints de staff (ej.
`/api/v1/pedidos/mis-pedidos` vs `/api/v1/pedidos` para el staff), así
que "la URL contiene `/cliente/`" no es una señal confiable — se probó y
causó un 403 real en producción. Cada servicio Angular que llama un
endpoint con `@PreAuthorize("hasRole('CLIENTE')")` en el backend debe
marcarlo explícitamente (ver `pedido-cliente.service.ts →
obtenerMisPedidos()` como ejemplo).

## `*appHasAccion`: cómo se decide qué botón se ve

```mermaid
sequenceDiagram
    participant U as Usuario (staff)
    participant L as LoginComponent
    participant A as AuthService
    participant Acc as AccesoService
    participant BE as Backend

    U->>L: usuario + clave
    L->>BE: POST /auth/login
    BE-->>L: JWT + datos de usuario
    L->>A: guardarSesion()
    A->>Acc: cargarMisAccesos()
    Acc->>BE: GET /accesos/mis-accesos
    BE-->>Acc: { modulos, codigosAccion: [...] }
    Note over Acc: signal con la lista de códigos
    Note over U: Cada *appHasAccion consulta<br/>ese signal, no un rol fijo
```

## Qué falta en el frontend
- UI de gestión de **clientes** en el panel interno (el backend ya expone
  el CRUD).
- Paginación en catálogo y lista de pedidos si crecen mucho.
- Redirección automática a `/cliente/login` cuando expira la sesión de
  cliente (hoy solo limpia el `localStorage`, sin redirect).
- Pantallas para el rol `REPARTIDOR` (depende de que el backend primero
  defina esos endpoints — ver nota en `02-estructura-backend.md`).
