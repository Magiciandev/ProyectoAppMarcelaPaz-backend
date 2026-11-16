package com.mpaz.contenidos.model;

import java.util.EnumSet;
import java.util.Set;

import com.mpaz.contenidos.exception.ForbiddenException;

/** Perfiles del sistema y los estados que cada uno puede ver (contrato, sección 3). */
public enum Rol {
    UTP(EnumSet.allOf(Estado.class)),
    DOCENTE(EnumSet.of(Estado.AUTORIZADO, Estado.PUBLICADO)),
    ESTUDIANTE(EnumSet.of(Estado.PUBLICADO));

    private final Set<Estado> estadosVisibles;

    Rol(Set<Estado> estadosVisibles) {
        this.estadosVisibles = estadosVisibles;
    }

    public boolean vee(Estado estado) {
        return estadosVisibles.contains(estado);
    }

    public static Rol de(String valor) {
        try {
            return Rol.valueOf(valor == null ? "" : valor);
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException("Rol no reconocido");
        }
    }
}
