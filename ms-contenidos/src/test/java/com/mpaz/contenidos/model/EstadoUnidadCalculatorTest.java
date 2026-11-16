package com.mpaz.contenidos.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class EstadoUnidadCalculatorTest {

    @Test
    void unidadSinObjetivosEsBorrador() {
        assertThat(EstadoUnidadCalculator.calcular(List.of())).isEqualTo(Estado.BORRADOR);
    }

    @Test
    void publicadoGanaSobreLosDemas() {
        assertThat(EstadoUnidadCalculator.calcular(
                List.of(Estado.BORRADOR, Estado.PUBLICADO, Estado.AUTORIZADO))).isEqualTo(Estado.PUBLICADO);
    }

    @Test
    void sinPublicadosGanaAutorizado() {
        assertThat(EstadoUnidadCalculator.calcular(
                List.of(Estado.EN_REVISION, Estado.AUTORIZADO))).isEqualTo(Estado.AUTORIZADO);
    }

    @Test
    void sinAutorizadosGanaEnRevision() {
        assertThat(EstadoUnidadCalculator.calcular(
                List.of(Estado.BORRADOR, Estado.EN_REVISION))).isEqualTo(Estado.EN_REVISION);
    }

    @Test
    void todosDeshabilitadosDeshabilitaLaUnidad() {
        assertThat(EstadoUnidadCalculator.calcular(
                List.of(Estado.DESHABILITADO, Estado.DESHABILITADO))).isEqualTo(Estado.DESHABILITADO);
    }

    @Test
    void mezclaDeBorradorYDeshabilitadoEsBorrador() {
        assertThat(EstadoUnidadCalculator.calcular(
                List.of(Estado.BORRADOR, Estado.DESHABILITADO))).isEqualTo(Estado.BORRADOR);
    }
}
