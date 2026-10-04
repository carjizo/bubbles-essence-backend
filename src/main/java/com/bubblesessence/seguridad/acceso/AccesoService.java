package com.bubblesessence.seguridad.acceso;

import com.bubblesessence.seguridad.acceso.dto.AccesoResponseDTO;

public interface AccesoService {

    /**
     * Resuelve el acceso EFECTIVO de un usuario: unión de las acciones de
     * todos sus grupos activos, más overrides puntuales (GRANT suma, DENY
     * resta), y los módulos se derivan de esas acciones. Esta es la única
     * fuente de verdad de "qué puede hacer" un usuario en el sistema.
     */
    AccesoResponseDTO resolverAcceso(Long usuarioId);
}
