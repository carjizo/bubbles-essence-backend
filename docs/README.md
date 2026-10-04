# Documentación técnica — Bubbles & Essence

| Archivo | Contenido |
|---|---|
| [`01-arquitectura-despliegue.md`](01-arquitectura-despliegue.md) | Infraestructura completa: GitHub, Render, Vercel, Supabase, variables de entorno y por qué de cada decisión |
| [`02-estructura-backend.md`](02-estructura-backend.md) | Módulos del backend, patrón de capas, matriz de autorización real por endpoint, flujo del JWT |
| [`03-estructura-frontend.md`](03-estructura-frontend.md) | Módulos del frontend, las dos sesiones (staff/cliente), patrón `*appHasAccion` |
| [`04-usuarios-casos-de-uso.md`](04-usuarios-casos-de-uso.md) | Actores del sistema, casos de uso por rol, flujo completo de un pedido |

Todos los diagramas están en [Mermaid](https://mermaid.js.org/) dentro de
bloques de código Markdown — se renderizan solos en GitHub (sin
instalar nada). Para editarlos, el [Live Editor de Mermaid](https://mermaid.live)
permite pegar el bloque y ver el resultado al instante.

> Generados a partir del código real del proyecto en esta fecha — si el
> sistema cambia bastante (nuevo módulo, nuevo rol con endpoints reales,
> etc.), conviene regenerarlos para que no queden desactualizados, igual
> que pasó con el README antes.
