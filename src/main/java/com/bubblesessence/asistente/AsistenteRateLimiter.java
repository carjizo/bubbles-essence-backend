package com.bubblesessence.asistente;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Límite simple por IP: máximo MAX_REQUESTS preguntas por VENTANA de
 * tiempo. Sin librerías externas (ni Redis, ni bucket4j) — un
 * ConcurrentHashMap en memoria alcanza de sobra a esta escala, y como
 * Render free corre una sola instancia, no hay problema de que el
 * contador no se comparta entre varias.
 *
 * LIMITACIÓN CONOCIDA: el contador vive en memoria, así que se reinicia
 * en cada deploy/restart, y si en el futuro escalas a varias instancias
 * (plan pago de Render con réplicas), cada una tendría su propio
 * contador -> el límite real efectivo se multiplicaría por el número de
 * instancias. En ese momento sí convendría migrar a un contador
 * compartido (Redis, o una tabla en Supabase). Para una sola instancia,
 * como ahora, esto es suficiente.
 */
@Component
public class AsistenteRateLimiter {

    private static final int MAX_REQUESTS = 10;
    private static final Duration VENTANA = Duration.ofMinutes(1);

    /** Cuánto tiempo sin actividad antes de limpiar una IP del mapa. */
    private static final Duration TTL_LIMPIEZA = Duration.ofHours(1);

    private record Contador(AtomicInteger conteo, AtomicReference<Instant> inicioVentana) {
    }

    private final ConcurrentHashMap<String, Contador> contadoresPorIp = new ConcurrentHashMap<>();

    /**
     * @return true si la IP todavía puede hacer requests, false si ya
     * superó el límite de la ventana actual.
     */
    public boolean permitir(String ip) {
        Contador contador = contadoresPorIp.computeIfAbsent(
                ip, k -> new Contador(new AtomicInteger(0), new AtomicReference<>(Instant.now())));

        Instant ahora = Instant.now();
        Instant inicioActual = contador.inicioVentana().get();

        if (Duration.between(inicioActual, ahora).compareTo(VENTANA) >= 0) {
            // La ventana venció: resetea el contador. El compareAndSet evita
            // una condición de carrera si dos requests llegan al mismo
            // tiempo justo cuando la ventana expira.
            if (contador.inicioVentana().compareAndSet(inicioActual, ahora)) {
                contador.conteo().set(0);
            }
        }

        return contador.conteo().incrementAndGet() <= MAX_REQUESTS;
    }

    /**
     * Sin esto, el mapa crecería para siempre con una entrada por cada IP
     * única que haya preguntado alguna vez. Se ejecuta cada 30 min y
     * borra las IPs inactivas hace más de 1 hora.
     */
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    public void limpiarEntradasViejas() {
        Instant limite = Instant.now().minus(TTL_LIMPIEZA);
        contadoresPorIp.entrySet().removeIf(e -> e.getValue().inicioVentana().get().isBefore(limite));
    }
}