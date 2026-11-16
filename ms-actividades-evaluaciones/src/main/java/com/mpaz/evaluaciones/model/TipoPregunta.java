package com.mpaz.evaluaciones.model;

import com.mpaz.evaluaciones.exception.BadRequestException;

/** Tipos de actividad y de pregunta (son los mismos cuatro del caso). */
public enum TipoPregunta {
    SELECCION, COMPLETACION, SECUENCIA, ASOCIACION;

    public static TipoPregunta de(String valor) {
        try {
            return TipoPregunta.valueOf(valor == null ? "" : valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo inválido: " + valor
                    + " (use SELECCION, COMPLETACION, SECUENCIA o ASOCIACION)");
        }
    }

    /** SELECCION y COMPLETACION usan la marca esCorrecta de las opciones. */
    public boolean usaEsCorrecta() {
        return this == SELECCION || this == COMPLETACION;
    }
}
