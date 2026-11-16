package com.mpaz.progreso.model;

import com.mpaz.progreso.exception.ForbiddenException;

/** Perfiles del sistema. */
public enum Rol {
    UTP, DOCENTE, ESTUDIANTE;

    public static Rol de(String valor) {
        try {
            return Rol.valueOf(valor == null ? "" : valor);
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException("Rol no reconocido");
        }
    }
}
