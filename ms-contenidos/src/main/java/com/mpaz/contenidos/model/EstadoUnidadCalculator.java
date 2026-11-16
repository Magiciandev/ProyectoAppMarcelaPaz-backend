package com.mpaz.contenidos.model;

import java.util.Collection;

/**
 * El estado de una unidad se deriva del de sus objetivos (contrato, sección 3):
 * PUBLICADO si alguno lo está; si no AUTORIZADO; si no EN_REVISION; si todos están
 * DESHABILITADO, DESHABILITADO; en cualquier otro caso BORRADOR.
 */
public final class EstadoUnidadCalculator {

    private EstadoUnidadCalculator() {
    }

    public static Estado calcular(Collection<Estado> estadosDeObjetivos) {
        if (estadosDeObjetivos.isEmpty()) {
            return Estado.BORRADOR;
        }
        if (estadosDeObjetivos.contains(Estado.PUBLICADO)) {
            return Estado.PUBLICADO;
        }
        if (estadosDeObjetivos.contains(Estado.AUTORIZADO)) {
            return Estado.AUTORIZADO;
        }
        if (estadosDeObjetivos.contains(Estado.EN_REVISION)) {
            return Estado.EN_REVISION;
        }
        boolean todosDeshabilitados = estadosDeObjetivos.stream().allMatch(e -> e == Estado.DESHABILITADO);
        return todosDeshabilitados ? Estado.DESHABILITADO : Estado.BORRADOR;
    }
}
