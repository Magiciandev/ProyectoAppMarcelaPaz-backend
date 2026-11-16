package com.mpaz.progreso.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NivelLogroTest {

    @Test
    void ochentaOMasDomina() {
        assertThat(NivelLogro.de(80)).isEqualTo(NivelLogro.DOMINA);
        assertThat(NivelLogro.de(100)).isEqualTo(NivelLogro.DOMINA);
    }

    @Test
    void entreSesentaYOchentaVaEnVias() {
        assertThat(NivelLogro.de(60)).isEqualTo(NivelLogro.EN_VIAS);
        assertThat(NivelLogro.de(79)).isEqualTo(NivelLogro.EN_VIAS);
    }

    @Test
    void bajoSesentaEsRefuerzo() {
        assertThat(NivelLogro.de(59)).isEqualTo(NivelLogro.REFUERZO);
        assertThat(NivelLogro.de(0)).isEqualTo(NivelLogro.REFUERZO);
    }
}
