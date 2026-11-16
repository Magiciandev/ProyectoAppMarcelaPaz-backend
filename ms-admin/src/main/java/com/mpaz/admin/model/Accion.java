package com.mpaz.admin.model;

import com.mpaz.admin.exception.BadRequestException;

/** Acciones de aprobación y la transición de estado que produce cada una (contrato, sección 3). */
public enum Accion {
    ENVIAR_REVISION(Estado.BORRADOR, Estado.EN_REVISION),
    AUTORIZAR(Estado.EN_REVISION, Estado.AUTORIZADO),
    RECHAZAR(Estado.EN_REVISION, Estado.BORRADOR),
    PUBLICAR(Estado.AUTORIZADO, Estado.PUBLICADO),
    DESHABILITAR(Estado.PUBLICADO, Estado.DESHABILITADO),
    REABRIR(Estado.DESHABILITADO, Estado.BORRADOR);

    private final Estado desde;
    private final Estado hasta;

    Accion(Estado desde, Estado hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public Estado desde() {
        return desde;
    }

    public Estado hasta() {
        return hasta;
    }

    public boolean permiteDesde(Estado actual) {
        return actual == desde;
    }

    public static Accion de(String valor) {
        try {
            return Accion.valueOf(valor == null ? "" : valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Acción inválida: " + valor);
        }
    }
}
