package com.mpaz.evaluaciones.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mpaz.evaluaciones.dto.OpcionDTO;
import com.mpaz.evaluaciones.exception.BadRequestException;

/**
 * Reglas de las opciones según el tipo de pregunta (contrato, sección 8):
 *  - SELECCION y COMPLETACION: al menos 2 opciones y exactamente 1 correcta.
 *  - SECUENCIA: al menos 2 opciones, todas con posicionCorrecta, sin repetir y de 1 a n.
 *  - ASOCIACION: cantidad par de opciones; cada parClave aparece exactamente dos veces.
 */
public final class ValidadorPregunta {

    private ValidadorPregunta() {
    }

    public static void validar(TipoPregunta tipo, List<OpcionDTO> opciones) {
        if (opciones == null || opciones.size() < 2) {
            throw new BadRequestException("Una pregunta necesita al menos 2 opciones");
        }
        switch (tipo) {
            case SELECCION, COMPLETACION -> validarUnaCorrecta(opciones);
            case SECUENCIA -> validarSecuencia(opciones);
            case ASOCIACION -> validarPares(opciones);
        }
    }

    private static void validarUnaCorrecta(List<OpcionDTO> opciones) {
        long correctas = opciones.stream().filter(o -> Boolean.TRUE.equals(o.getEsCorrecta())).count();
        if (correctas != 1) {
            throw new BadRequestException("Debe haber exactamente 1 opción correcta (hay " + correctas + ")");
        }
    }

    private static void validarSecuencia(List<OpcionDTO> opciones) {
        Set<Integer> posiciones = new HashSet<>();
        for (OpcionDTO o : opciones) {
            if (o.getPosicionCorrecta() == null) {
                throw new BadRequestException("En una secuencia todas las opciones llevan posicionCorrecta");
            }
            if (!posiciones.add(o.getPosicionCorrecta())) {
                throw new BadRequestException("La posicionCorrecta no puede repetirse");
            }
        }
        for (int i = 1; i <= opciones.size(); i++) {
            if (!posiciones.contains(i)) {
                throw new BadRequestException("Las posiciones deben ir de 1 a " + opciones.size() + " sin saltos");
            }
        }
    }

    private static void validarPares(List<OpcionDTO> opciones) {
        if (opciones.size() % 2 != 0) {
            throw new BadRequestException("En una asociación la cantidad de opciones debe ser par");
        }
        Map<String, Integer> conteo = new HashMap<>();
        for (OpcionDTO o : opciones) {
            if (o.getParClave() == null || o.getParClave().isBlank()) {
                throw new BadRequestException("En una asociación todas las opciones llevan parClave");
            }
            conteo.merge(o.getParClave().trim(), 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : conteo.entrySet()) {
            if (e.getValue() != 2) {
                throw new BadRequestException("Cada parClave debe aparecer exactamente dos veces ('" + e.getKey() + "')");
            }
        }
    }
}
