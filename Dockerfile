# syntax=docker/dockerfile:1

# ---------- Etapa 1: build ----------
# Imagen con Gradle + JDK 21 solo para compilar. No viaja a producción.
FROM gradle:8.10-jdk21 AS build
WORKDIR /build

# Copiamos primero SOLO el build.gradle (y settings.gradle si lo tienes) y
# descargamos dependencias. Mientras no cambien, Docker reutiliza esta capa
# cacheada y no vuelve a bajar todo Maven Central en cada build.
COPY build.gradle .
COPY settings.gradle* .
RUN gradle dependencies --no-daemon || true

# Ahora sí copiamos el código y compilamos.
COPY src ./src
RUN gradle clean build -x test --no-daemon

# ---------- Etapa 2: runtime ----------
# Imagen final: solo JRE (no JDK, no Maven) -> mucho más liviana.
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuario no-root por seguridad (buena práctica para cualquier imagen en prod).
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copiamos el jar ya compilado desde la etapa de build.
# (requiere el ajuste en build.gradle que deshabilita el jar "plain",
# ver instrucciones aparte -> así queda un solo .jar en build/libs/)
COPY --from=build /build/build/libs/*.jar app.jar

# Render asigna el puerto real vía la env var PORT (ver application.yml).
# Este EXPOSE es documental, no fuerza nada.
EXPOSE 8080

# -XX:MaxRAMPercentage evita que la JVM asuma que tiene toda la RAM del host,
# importante en los planes free/starter de Render que vienen con poca memoria.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
