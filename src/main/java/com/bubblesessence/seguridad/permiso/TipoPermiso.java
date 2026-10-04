package com.bubblesessence.seguridad.permiso;

/**
 * GRANT: le doy la acción al usuario aunque su(s) grupo(s) no la tengan.
 * DENY: le quito la acción al usuario aunque su(s) grupo(s) sí la tengan.
 * Es la excepción puntual; el día a día se maneja con Grupo + GrupoAccion.
 */
public enum TipoPermiso {
    GRANT,
    DENY
}
