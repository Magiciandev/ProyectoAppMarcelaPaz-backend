package com.mpaz.evaluaciones.model;

import com.mpaz.evaluaciones.exception.BadRequestException;

/** Ciclo de vida de un objetivo de aprendizaje y de lo que depende de él. */
public enum Estado {
    BORRADOR, EN_REVISION, AUTORIZADO, PUBLICADO, DESHABILITADO;

    public static Estado de(String valor) {
        try {
            return Estado.valueOf(valor == null ? "" : valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + valor);
        }
    }
}
