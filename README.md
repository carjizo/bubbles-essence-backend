# bubbles-essence — Backend

API REST para el negocio de jabones artesanales de glicerina (Bubbles & Essence).

Cubre, por ahora, estas tablas de `bd_bubbles_essence`:

| Tabla                              | Esquema  | Módulo                          |
|-------------------------------------|----------|----------------------------------|
| `tbl_seg_usuario`                   | grp_seg  | `seguridad.usuario`               |
| —  (login, sin tabla propia)        | —        | `seguridad.auth`                  |
| `tbl_mge_ingrediente`               | grp_mge  | `maestros.ingrediente`            |
| `tbl_mge_cliente`                   | grp_mge  | `maestros.cliente`                |
| `tbl_ven_producto`                  | grp_ven  | `ventas.producto`                 |
| `tbl_ven_productoingrediente`       | grp_ven  | `ventas.productoingrediente`      |
| `tbl_ven_pedido` + `tbl_ven_pedidodetalle` | grp_ven | `ventas.pedido`             |

El resto de tablas del script (pago, incidencia, lote, entrega, etc.)
**no** se implementan todavía — se agregan como módulos nuevos cuando toque,
siguiendo exactamente el mismo patrón.

## 1. Por qué esta arquitectura

Se organizó **por módulo de negocio** (feature-based), no por capa técnica
plana, porque así:

- Cada módulo (`seguridad`, `maestros`, `ventas`) es autocontenido: si mañana
  agregas `tbl_ven_pedido`, creas `ventas/pedido/` con sus propias clases y no
  tocas nada de los módulos existentes.
- Dentro de cada módulo sí se respeta la separación de capas:

  ```
  ventas/producto/
    Producto.java              (entity - capa de persistencia)
    ProductoRepository.java    (capa de acceso a datos)
    ProductoService.java       (contrato de la capa de negocio)
    ProductoServiceImpl.java   (implementación de la capa de negocio)
    ProductoMapper.java        (traduce Entity <-> DTO)
    ProductoController.java    (capa de exposición HTTP)
    dto/
      ProductoRequestDTO.java
      ProductoResponseDTO.java
  ```

- `common/` tiene lo transversal a todos los módulos: manejo de errores
  (`GlobalExceptionHandler`), el wrapper de respuesta (`ApiResponse<T>`) y
  configuración (Swagger, encoder de contraseñas).

## 2. Decisiones clave

- **Sin Flyway/Liquibase**: la base de datos la creas tú a mano con tu script
  SQL. `ddl-auto: validate` en `application.yml` hace que Hibernate solo
  **verifique** que las entidades calcen con las tablas reales; nunca las
  modifica. Si algún día cambias el script SQL y las entidades no calzan, la
  app falla al arrancar (a propósito, para detectarlo temprano).

- **Nombres de columna explícitos (`@Column(name = "...")`)**: tu script no
  usa comillas en los identificadores (`cpnID_Ingrediente`), así que Postgres
  los guarda en minúsculas tal cual, sin insertar guiones bajos
  (`cpnid_ingrediente`, NO `cpn_id_ingrediente`). Por eso cada entidad mapea
  el nombre real de columna a mano en vez de confiar en la estrategia de
  nombres por defecto de Hibernate (que sí inserta guiones bajos y rompería
  el mapeo).

- **DTOs de entrada y salida separados**: nunca se expone la entidad JPA
  directamente. Esto evita, por ejemplo, que `cpcClaveHash` del usuario
  viaje en una respuesta JSON.

- **`ApiResponse<T>`**: toda respuesta (éxito o error) tiene la forma
  `{ success, message, data, timestamp }`, para que el consumo desde
  Postman/Angular sea predecible.

- **Baja lógica (soft delete)** en `usuario`, `ingrediente` y `producto`
  (tienen `cpbActivo`): el `DELETE` del controller en realidad marca
  `activo = false`, no borra la fila. `producto_ingrediente` (línea de
  receta) sí se borra físicamente porque no tiene ese flag y no aporta
  mantener registros "muertos" ahí.

- **Contraseña**: el `UsuarioRequestDTO` recibe `clave` en texto plano solo
  para la petición; el servicio la hashea con BCrypt antes de guardarla.
  Nunca se devuelve en ninguna respuesta.

## 3. Autenticación y roles (personal interno)

Ya se agregó `spring-boot-starter-security` + JWT (`jjwt`). Flujo:

1. `POST /api/v1/auth/login` con `{ "usuario": "...", "clave": "..." }` →
   responde `{ token, tipoToken: "Bearer", id, nombreCompleto, usuario, rol }`.
2. En cada request protegido, mandas el header
   `Authorization: Bearer <token>`.
3. `JwtAuthenticationFilter` valida el token y arma el `SecurityContext` con
   el rol del usuario como authority `ROLE_<ROL>` (`ROLE_ADMIN`,
   `ROLE_OPERADOR`, `ROLE_VENDEDOR`, `ROLE_REPARTIDOR`).
4. Cada controller decide, con `@PreAuthorize`, qué rol(es) puede tocar cada
   endpoint. Reglas actuales:

| Módulo | Lectura (GET) | Escritura (POST/PUT/PATCH/DELETE) |
|---|---|---|
| Usuarios | ADMIN | ADMIN |
| Clientes | ADMIN, OPERADOR, VENDEDOR | ADMIN, OPERADOR, VENDEDOR |
| Productos / Ingredientes / Receta | **Público** (invitados incluidos) | ADMIN, OPERADOR |
| Pedidos (listar todos, detalle por id) | ADMIN, OPERADOR, VENDEDOR | — |
| Pedidos (cambiar estado) | — | ADMIN, OPERADOR |
| Pedidos (cancelar, como staff) | — | ADMIN, OPERADOR |

No hay endpoint de "registro" de usuarios internos: los crea un ADMIN desde
`POST /api/v1/usuarios`. El primer ADMIN lo insertas tú a mano en la base
(con la clave ya hasheada en BCrypt) o creas un usuario temporal, generas su
hash con un endpoint de prueba y lo pegas directo en la tabla — como
prefieras, es un caso único de bootstrap.

## 4. Invitados (pedidos sin cuenta)

Un cliente **nunca necesita loguearse** para comprar. Tres rutas quedan
100% públicas (ver `SecurityConfig`):

| Método | Ruta | Qué hace |
|---|---|---|
| `POST` | `/api/v1/pedidos` | Crea el pedido. Envía `invitadoNombre` + `invitadoTelefono` (no `clienteId`). |
| `GET` | `/api/v1/pedidos/seguimiento?codigoPedido=...&telefono=...` | Consulta el estado. El teléfono debe coincidir con el del pedido — así nadie puede ver pedidos ajenos solo adivinando el código. |
| `PATCH` | `/api/v1/pedidos/seguimiento/cancelar` | Cancela su propio pedido (mismo par código + teléfono), solo si el pedido no está `SHIPPED`, `DELIVERED` ni ya `CANCELLED`. |

El `codigoPedido` (ej. `PED-2026-000123`) se genera solo al crear el pedido
y es lo único que el invitado necesita guardar (junto con su teléfono) para
todo el seguimiento — nunca necesita saber el `id` interno.

Si en el futuro quieres que un cliente recurrente se "registre" y tenga
`Mis pedidos` con historial, ese es el flujo de `Cliente` (`clienteId`)
en vez de invitado — la lógica de creación de pedido ya soporta ambos
casos (ver `PedidoRequestDTO`, son mutuamente excluyentes).

## 5. Módulos, acciones, grupos y permisos (mantenimiento)

Esto es lo que le da al front (Angular) todo lo que necesita para mostrar/
ocultar menús y botones por usuario, y lo que te permite "activar usuarios,
dar permisos a módulos y acciones" desde una pantalla de administración.

### El modelo

Todo el permiso se apoya en un solo concepto: la **Acción** (`tbl_seg_accion`,
ej. código `btn-editar-pedido`), que siempre pertenece a un **Módulo**
(`tbl_seg_modulo`, ej. `PEDIDOS`). No existe una tabla separada de "acceso a
módulo": un usuario tiene acceso a un módulo si tiene AL MENOS UNA acción de
ese módulo — así no hay dos fuentes de verdad para mantener sincronizadas.

```
Modulo (1) ───< Accion (N)
Grupo  (N) ───< GrupoAccion >─── (N) Accion       (qué puede hacer un grupo)
Usuario(N) ───< UsuarioGrupo >─── (N) Grupo        (a qué grupos pertenece)
Usuario(N) ───< UsuarioAccion >─── (N) Accion      (override puntual GRANT/DENY)
```

- **Grupo** (`tbl_seg_grupo`): el día a día. Ej. "VENTAS" tiene
  `btn-ver-pedido`, `btn-editar-cliente`; "ADMINISTRADORES" tiene todo.
- **UsuarioAccion** (override puntual): la excepción. `GRANT` le da a un
  usuario específico una acción aunque su grupo no la tenga; `DENY` se la
  quita aunque su grupo sí la tenga. El override **siempre gana** sobre lo
  que dan los grupos (ver `AccesoServiceImpl`).

### El endpoint que consume el front

`GET /api/v1/accesos/mis-accesos` (cualquier usuario logueado, sobre sí
mismo) devuelve exactamente lo que Angular necesita:

```json
{
  "data": {
    "usuarioId": 3,
    "usuario": "pedro",
    "nombreCompleto": "Pedro Ramirez",
    "rol": "OPERADOR",
    "activo": true,
    "grupos": [ { "id": 3, "codigo": "OPERACIONES", "nombre": "Equipo de operaciones" } ],
    "modulos": [ { "id": 3, "codigo": "PEDIDOS", "nombre": "Pedidos", "orden": 3 } ],
    "acciones": ["btn-ver-pedido", "btn-editar-pedido", "btn-cancelar-pedido"]
  }
}
```

En Angular, mostrar un botón se reduce a:
`this.accesos.acciones.includes('btn-editar-pedido')`. El front no necesita
saber nada de roles, grupos ni reglas de negocio — solo esa lista de strings.

Para que un ADMIN pueda simular/inspeccionar el acceso de otro usuario (útil
en la propia pantalla de administración de permisos), existe también
`GET /api/v1/accesos/usuarios/{usuarioId}`.

### Endpoints de mantenimiento (todos ADMIN)

| Recurso | Base path | Extra |
|---|---|---|
| Módulos | `/api/v1/modulos` | `PATCH /{id}/activar`, `PATCH /{id}/desactivar` |
| Acciones | `/api/v1/acciones` | `?moduloId=` para filtrar |
| Grupos | `/api/v1/grupos` | `PATCH /{id}/activar`, `/desactivar` |
| Acciones de un grupo | `/api/v1/grupos/{id}/acciones` | `POST/DELETE /{accionId}` para asignar/quitar |
| Usuarios de un grupo | `/api/v1/grupos/{id}/usuarios` | `POST/DELETE /{usuarioId}` para asignar/quitar |
| Permisos puntuales de un usuario | `/api/v1/usuarios/{usuarioId}/permisos` | `POST` con `{accionId, tipo: GRANT\|DENY}`, `DELETE /{accionId}` |
| Activar/desactivar usuario | `/api/v1/usuarios/{id}/activar` \| `DELETE /api/v1/usuarios/{id}` (desactiva) | |

### SQL de estas tablas

Están en `sql/grp_seg_permisos.sql` (corre DESPUÉS de tu
`bd_bubbles_essence.sql` original, reutiliza sus dominios `tddu_*` y su
esquema `grp_seg`). Trae datos semilla: 4 módulos, 8 acciones de ejemplo y
3 grupos (con "ADMINISTRADORES" ya con todas las acciones asignadas) para
que puedas probar el flujo completo sin armar todo a mano.

## 6. Autenticación desacoplada de la autorización (para cuando llegue Cognito)

A propósito, el JWT actual (`/api/v1/auth/login`) **solo prueba identidad**:
usuario existe, está activo, la clave es correcta. No mete lógica de
permisos adentro del token (más allá del `rol`, que se mantiene por
compatibilidad con los `@PreAuthorize(hasRole(...))` que ya existían antes
de este módulo). Toda la autorización fina vive en las tablas de la sección
5, y se resuelve siempre en el momento de la consulta (`AccesoService`),
nunca "cacheada" dentro del token.

Esto es intencional: cuando reemplaces el login propio por AWS Cognito (o
cualquier otro IdP), el cambio es acotado a UN filtro nuevo:

1. Escribes un `CognitoJwtAuthenticationFilter` que valida el JWT de
   Cognito contra su JWKS público (librería `spring-security-oauth2-resource-server`
   simplifica esto).
2. Con la identidad ya validada (el `sub` o correo del token de Cognito),
   buscas el `Usuario` correspondiente en `tbl_seg_usuario` (puede que
   quieras agregar una columna `cpcCognitoSub` para el match) y armas el
   mismo `UsuarioPrincipal` que ya arma `JwtAuthenticationFilter` hoy.
3. **Nada más cambia**: `SecurityConfig`, `AccesoService`, `GrupoController`,
   y todos los `@PreAuthorize` siguen funcionando igual, porque nunca
   dependieron de cómo se autenticó la request, solo del `Authentication`
   ya resuelto.

Mientras tanto, para llamadas máquina-a-máquina (o para probar sin manejar
un JWT), existe una alternativa más simple: el header `X-API-KEY`. Está
deshabilitada por defecto (variable `API_KEY` vacía); si la configuras,
cualquier request con `X-API-KEY: <ese valor>` se autentica como
`ROLE_SYSTEM`, sin pasar por login. Ver `ApiKeyAuthenticationFilter`.

## 7. Cómo correrlo

### Requisitos
- Java 21
- Gradle: no necesitas instalarlo, el proyecto trae el wrapper (`./gradlew`)
- PostgreSQL con `bd_bubbles_essence` ya creada (corre tu script `.sql` antes)

### Variables de entorno

`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD` **son obligatorias**
(el `application.yml` ya no trae defaults para la conexión — así evitamos
que alguien arranque "sin darse cuenta" contra una base equivocada). El
resto sí tiene default pensado para desarrollo local:

| Variable      | Obligatoria | Default (si no la seteas) |
|---------------|-------------|-----------------------------|
| `DB_HOST`     | Sí          | — (falla al arrancar)        |
| `DB_PORT`     | Sí          | — (falla al arrancar)        |
| `DB_NAME`     | Sí          | — (falla al arrancar)        |
| `DB_USER`     | Sí          | — (falla al arrancar)        |
| `DB_PASSWORD` | Sí          | — (falla al arrancar)        |
| `JWT_SECRET`  | No          | clave de desarrollo incluida (**cámbiala en producción**) |
| `JWT_EXPIRATION_MINUTES` | No | `480` (8 horas) |
| `API_KEY`     | No          | vacío = deshabilitado (ver sección 6) |

Forma rápida de correrlo en local (bash/zsh):

```bash
export DB_HOST=localhost DB_PORT=5432 DB_NAME=bd_bubbles_essence DB_USER=postgres DB_PASSWORD=postgres
./gradlew bootRun
```

La API queda en `http://localhost:8080`.
Swagger disponible en `http://localhost:8080/swagger-ui.html`.

## 8. Endpoints para probar en Postman

Importa `postman/bubbles-essence.postman_collection.json` (trae login,
usuarios, módulos/acciones/grupos/permisos, accesos, clientes, catálogo y
pedidos, con `{{baseUrl}} = http://localhost:8080` y una variable `{{token}}`
que puedes llenar con el token del login). Los endpoints de módulos,
acciones, grupos, permisos y accesos ya están documentados en la sección 5.

### Auth — `/api/v1/auth`
| Método | Ruta      | Descripción                    |
|--------|-----------|----------------------------------|
| POST   | `/login`  | Login de personal interno → JWT  |

### Usuarios — `/api/v1/usuarios`
| Método | Ruta            | Descripción                          |
|--------|-----------------|----------------------------------------|
| GET    | `/`             | Lista (filtro opcional `?activo=true`) |
| GET    | `/{id}`         | Detalle                                |
| POST   | `/`             | Crear                                  |
| PUT    | `/{id}`         | Actualizar                             |
| DELETE | `/{id}`         | Desactivar (soft delete)               |

### Ingredientes — `/api/v1/ingredientes`
Mismos 5 endpoints que usuarios.

### Productos — `/api/v1/productos`
Mismos 5 endpoints que usuarios.

### Receta (Producto-Ingrediente) — `/api/v1/producto-ingredientes`
| Método | Ruta                          | Descripción                              |
|--------|-------------------------------|--------------------------------------------|
| GET    | `/?productoId={id}`           | Receta de un producto (o todas si se omite)|
| GET    | `/{id}`                       | Detalle de una línea de receta             |
| POST   | `/`                           | Agregar un ingrediente a la receta (ADMIN/OPERADOR) |
| PUT    | `/{id}`                       | Actualizar una línea de receta (ADMIN/OPERADOR) |
| DELETE | `/{id}`                       | Quitar el ingrediente de la receta (ADMIN/OPERADOR) |

### Clientes — `/api/v1/clientes` (ADMIN, OPERADOR, VENDEDOR)
CRUD estándar (`GET /`, `GET /{id}`, `POST /`, `PUT /{id}`), sin soft delete.

### Pedidos — `/api/v1/pedidos`
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/` | **público** | Crea el pedido (invitado o cliente con cuenta) |
| GET | `/seguimiento?codigoPedido=&telefono=` | **público** | Estado del pedido del invitado |
| PATCH | `/seguimiento/cancelar` | **público** | El invitado cancela su propio pedido |
| GET | `/?estado=` | staff | Lista todos los pedidos (filtro opcional por estado) |
| GET | `/{id}` | staff | Detalle de cualquier pedido |
| PATCH | `/{id}/estado` | staff | Cambia el estado (ver `EstadoPedido`) |
| PATCH | `/{id}/cancelar` | staff | Cancela con motivo operativo |

### Flujo sugerido para probar de punta a punta
1. `POST /api/v1/ingredientes` → crea 2-3 ingredientes (ej. "Cúrcuma", "Miel").
2. `POST /api/v1/productos` → crea un jabón (ej. "Jabón Cúrcuma y Miel", precio 11.00).
3. `POST /api/v1/producto-ingredientes` → asocia cada ingrediente al producto
   creado (`productoId`, `ingredienteId`, `cantidadReferencial`: "10 g").
4. `GET /api/v1/producto-ingredientes?productoId={id}` → ves la receta completa.
5. `POST /api/v1/pedidos` **sin token**, como invitado (`invitadoNombre`,
   `invitadoTelefono`, `tipoEntrega`, `items: [{productoId, cantidad}]`)
   → guarda el `codigoPedido` que devuelve.
6. `GET /api/v1/pedidos/seguimiento?codigoPedido=...&telefono=...` **sin
   token** → confirmas que el invitado ve su pedido.
7. Para probar el lado staff: necesitas un usuario ADMIN ya creado en la
   base (ver sección 3, bootstrap). `POST /api/v1/auth/login` → copia el
   `token` → en Postman, pégalo en la variable `{{token}}` → prueba
   `GET /api/v1/pedidos` y `PATCH /api/v1/pedidos/{id}/estado`.

## 9. Despliegue (Docker + Render)

El backend corre en Render como contenedor Docker, conectado a Postgres en
Supabase. `Dockerfile` usa build multi-stage (Gradle+JDK solo para compilar,
JRE puro para correr → imagen final liviana) y `render.yaml` es un Blueprint
que automatiza la creación del servicio en Render a partir del repo.

**Pendiente antes del primer deploy**: `server.port` en `application.yml`
sigue fijo en `8080`. Render inyecta el puerto real vía la variable de
entorno `PORT`, así que hay que cambiarlo a:

```yaml
server:
  port: ${PORT:8080}
```

(el `:8080` es solo el fallback para seguir corriendo normal en local).

### Variables de entorno en Render
Las mismas de la sección 7, seteadas en el dashboard de Render (o vía
`render.yaml` con `sync: false` para que las pida una sola vez). Usa el
host de **conexión directa** de Supabase, no el connection pooler de
transacciones — Hibernate/JPA necesita sesiones persistentes.

### Checklist antes de hacer push
- [ ] `server.port: ${PORT:8080}` aplicado (punto de arriba).
- [ ] `/swagger-ui.html`, `/swagger-ui/**` y `/v3/api-docs/**` en `permitAll()`
      dentro de `SecurityConfig` — Render usa `/swagger-ui.html` como
      healthcheck (`render.yaml`); si queda protegido por JWT, Render va a
      marcar el deploy como fallido en loop.
- [ ] CORS en `SecurityConfig`/`CorsConfig` incluye el dominio real donde
      quede publicado el frontend (Angular), no solo `localhost`.

## 10. Próximos módulos (cuando quieras seguir)

Con roles + invitados ya listos para conectar el front, lo que sigue en el
backend, siguiendo el mismo patrón de carpetas:
`ven.pago` (el caso "Yape no llegó") → `ope.incidencia` + `ope.notapedido`
→ `ope.lote` + `log.movimientoinventario` (reservar/liberar stock al crear
o cancelar un pedido) → `log.entrega`.
