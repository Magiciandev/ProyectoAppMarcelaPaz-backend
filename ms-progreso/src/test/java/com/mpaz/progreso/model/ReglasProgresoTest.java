package com.mpaz.progreso.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class ReglasProgresoTest {

    private Asignacion asignacion(LocalDate desde, LocalDate hasta, LocalDate habilitacion, LocalDate rendicion) {
        return new Asignacion(1L, 3L, 1L, null, 2L, true, desde, hasta, 3, habilitacion, rendicion, Instant.now());
    }

    // ---------- porcentaje y promedio ----------

    @Test
    void calculaElPorcentajeRedondeado() {
        assertThat(ReglasProgreso.porcentaje(new BigDecimal("3"), new BigDecimal("4"))).isEqualTo(75);
        assertThat(ReglasProgreso.porcentaje(new BigDecimal("1"), new BigDecimal("3"))).isEqualTo(33);
        assertThat(ReglasProgreso.porcentaje(new BigDecimal("2"), new BigDecimal("3"))).isEqualTo(67);
        assertThat(ReglasProgreso.porcentaje(BigDecimal.ZERO, new BigDecimal("4"))).isEqualTo(0);
        assertThat(ReglasProgreso.porcentaje(new BigDecimal("4"), new BigDecimal("4"))).isEqualTo(100);
    }

    @Test
    void promedioRedondeadoYNuloSiNoHayDatos() {
        assertThat(ReglasProgreso.promedio(List.of(100, 50))).isEqualTo(75);
        assertThat(ReglasProgreso.promedio(List.of(70, 71))).isEqualTo(71);
        assertThat(ReglasProgreso.promedio(List.of())).isNull();
    }

    // ---------- ventanas de tiempo ----------

    @Test
    void sinFechasDePruebaMandaLaVentanaDeLaAsignacion() {
        Asignacion a = asignacion(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31), null, null);

        assertThat(ReglasProgreso.fueraDeVentana(a, LocalDate.of(2026, 11, 1))).isFalse();
        assertThat(ReglasProgreso.fueraDeVentana(a, LocalDate.of(2026, 12, 31))).isFalse();
        assertThat(ReglasProgreso.fueraDeVentana(a, LocalDate.of(2026, 10, 31))).isTrue();
        assertThat(ReglasProgreso.fueraDeVentana(a, LocalDate.of(2027, 1, 1))).isTrue();
    }

    @Test
    void laPruebaEstrechaLaVentana() {
        Asignacion a = asignacion(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 27));

        assertThat(ReglasProgreso.ventanaDesde(a)).isEqualTo(LocalDate.of(2026, 11, 20));
        assertThat(ReglasProgreso.ventanaHasta(a)).isEqualTo(LocalDate.of(2026, 11, 27));
        assertThat(ReglasProgreso.fueraDeVentana(a, LocalDate.of(2026, 11, 19))).isTrue();
        assertThat(ReglasProgreso.fueraDeVentana(a, LocalDate.of(2026, 11, 20))).isFalse();
        assertThat(ReglasProgreso.fueraDeVentana(a, LocalDate.of(2026, 11, 28))).isTrue();
    }

    @Test
    void lasFechasDeLaPruebaNoAmplianLaAsignacion() {
        Asignacion a = asignacion(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 1));

        assertThat(ReglasProgreso.ventanaDesde(a)).isEqualTo(LocalDate.of(2026, 11, 10));
        assertThat(ReglasProgreso.ventanaHasta(a)).isEqualTo(LocalDate.of(2026, 11, 20));
    }

    // ---------- reinicios ----------

    @Test
    void unReinicioPosteriorDejaSinEfectoLoAnterior() {
        Instant reinicio = Instant.parse("2026-11-16T14:30:00Z");
        List<ReinicioProgreso> reinicios = List.of(new ReinicioProgreso(1L, 3L, 7L, 2L, reinicio, null));

        assertThat(ReglasProgreso.anteriorAlReinicio(reinicios, 7L, Instant.parse("2026-11-16T14:00:00Z"))).isTrue();
        assertThat(ReglasProgreso.anteriorAlReinicio(reinicios, 7L, Instant.parse("2026-11-16T15:00:00Z"))).isFalse();
    }

    @Test
    void unReinicioDeOtroObjetivoNoAfecta() {
        List<ReinicioProgreso> reinicios = List.of(
                new ReinicioProgreso(1L, 3L, 7L, 2L, Instant.parse("2026-11-16T14:30:00Z"), null));

        assertThat(ReglasProgreso.anteriorAlReinicio(reinicios, 8L, Instant.parse("2026-11-16T14:00:00Z"))).isFalse();
    }

    @Test
    void unReinicioGlobalAfectaATodosLosObjetivos() {
        List<ReinicioProgreso> reinicios = List.of(
                new ReinicioProgreso(1L, 3L, null, 2L, Instant.parse("2026-11-16T14:30:00Z"), null));

        assertThat(ReglasProgreso.anteriorAlReinicio(reinicios, 8L, Instant.parse("2026-11-16T14:00:00Z"))).isTrue();
    }
}
