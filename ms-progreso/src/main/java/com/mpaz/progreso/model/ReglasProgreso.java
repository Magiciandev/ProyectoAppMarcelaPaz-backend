package com.mpaz.progreso.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/** Reglas de cálculo y de ventanas de tiempo (lógica pura, fácil de probar). */
public final class ReglasProgreso {

    private ReglasProgreso() {
    }

    /** Porcentaje entero (redondeado hacia arriba desde .5) de puntaje obtenido sobre puntaje máximo. */
    public static int porcentaje(BigDecimal obtenido, BigDecimal maximo) {
        return obtenido.multiply(BigDecimal.valueOf(100))
                .divide(maximo, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /** Promedio entero redondeado, o null si no hay datos. */
    public static Integer promedio(Collection<Integer> valores) {
        if (valores.isEmpty()) {
            return null;
        }
        double suma = 0;
        for (Integer v : valores) {
            suma += v;
        }
        return (int) Math.round(suma / valores.size());
    }

    /** Primer día en que se puede rendir: lo más tardío entre el inicio de la asignación y la habilitación de la prueba. */
    public static LocalDate ventanaDesde(Asignacion a) {
        LocalDate desde = a.getFechaDesde();
        if (a.getFechaHabilitacion() != null && a.getFechaHabilitacion().isAfter(desde)) {
            desde = a.getFechaHabilitacion();
        }
        return desde;
    }

    /** Último día en que se puede rendir: lo más temprano entre el fin de la asignación y la rendición de la prueba. */
    public static LocalDate ventanaHasta(Asignacion a) {
        LocalDate hasta = a.getFechaHasta();
        if (a.getFechaRendicion() != null && a.getFechaRendicion().isBefore(hasta)) {
            hasta = a.getFechaRendicion();
        }
        return hasta;
    }

    public static boolean fueraDeVentana(Asignacion a, LocalDate fecha) {
        return fecha.isBefore(ventanaDesde(a)) || fecha.isAfter(ventanaHasta(a));
    }

    /** True si un reinicio posterior (del mismo objetivo o de todos) deja sin efecto lo hecho en ese momento. */
    public static boolean anteriorAlReinicio(List<ReinicioProgreso> reinicios, Long objetivoId, Instant momento) {
        for (ReinicioProgreso r : reinicios) {
            boolean aplica = r.getObjetivoAprendizajeId() == null || r.getObjetivoAprendizajeId().equals(objetivoId);
            if (aplica && r.getFecha().isAfter(momento)) {
                return true;
            }
        }
        return false;
    }
}
