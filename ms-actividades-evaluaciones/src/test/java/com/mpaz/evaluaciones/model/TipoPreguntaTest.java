package com.mpaz.evaluaciones.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.mpaz.evaluaciones.exception.BadRequestException;

class TipoPreguntaTest {

    @Test
    void interpretaElTextoSinImportarMayusculas() {
        assertThat(TipoPregunta.de(" secuencia ")).isEqualTo(TipoPregunta.SECUENCIA);
    }

    @Test
    void tipoDesconocidoEsPeticionInvalida() {
        assertThatThrownBy(() -> TipoPregunta.de("DIBUJO")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> TipoPregunta.de(null)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void soloSeleccionYCompletacionUsanEsCorrecta() {
        assertThat(TipoPregunta.SELECCION.usaEsCorrecta()).isTrue();
        assertThat(TipoPregunta.COMPLETACION.usaEsCorrecta()).isTrue();
        assertThat(TipoPregunta.SECUENCIA.usaEsCorrecta()).isFalse();
        assertThat(TipoPregunta.ASOCIACION.usaEsCorrecta()).isFalse();
    }
}
