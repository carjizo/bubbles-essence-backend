package com.bubblesessence.asistente;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Info general del negocio (envíos, recojo, pago, contacto) que el
 * asistente puede usar para responder sin inventar nada. Vive en
 * application.yml (app.asistente.info-tienda) en vez de hardcodeada acá,
 * para que se pueda actualizar un dato (ej. el WhatsApp, un horario)
 * sin tener que tocar código Java ni redeployar con un cambio de texto
 * perdido entre el resto del prompt.
 */
@Service
public class InfoTiendaService {

    @Value("${app.asistente.info-tienda}")
    private String infoTienda;

    public String obtenerInfo() {
        return infoTienda;
    }
}