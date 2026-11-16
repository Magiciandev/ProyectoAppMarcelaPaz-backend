package com.mpaz.progreso.model;

/**
 * Nivel de logro según el mejor porcentaje: regla fija, sin diagnóstico automático.
 * DOMINA desde 80 %, EN_VIAS desde 60 %, REFUERZO por debajo.
 */
public enum NivelLogro {
    DOMINA, EN_VIAS, REFUERZO;

    public static NivelLogro de(int porcentaje) {
        if (porcentaje >= 80) {
            return DOMINA;
        }
        if (porcentaje >= 60) {
            return EN_VIAS;
        }
        return REFUERZO;
    }
}
