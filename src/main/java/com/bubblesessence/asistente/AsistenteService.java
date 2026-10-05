package com.bubblesessence.asistente;

import com.bubblesessence.asistente.dto.PreguntaChatDTO;
import com.bubblesessence.asistente.dto.RespuestaChatDTO;

public interface AsistenteService {

    RespuestaChatDTO responder(PreguntaChatDTO pregunta);
}
