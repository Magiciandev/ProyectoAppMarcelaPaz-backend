package com.mpaz.evaluaciones.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mpaz.evaluaciones.dto.OpcionDTO;
import com.mpaz.evaluaciones.exception.BadRequestException;

class ValidadorPreguntaTest {

    private OpcionDTO op(String texto, Boolean correcta, Integer posicion, String par) {
        return new OpcionDTO(null, texto, correcta, posicion, par, null);
    }

    // ---------- selección y completación ----------

    @Test
    void seleccionConUnaCorrectaEsValida() {
        List<OpcionDTO> opciones = List.of(op("a", true, null, null), op("b", false, null, null));
        assertThatCode(() -> ValidadorPregunta.validar(TipoPregunta.SELECCION, opciones)).doesNotThrowAnyException();
    }

    @Test
    void completacionUsaLaMismaRegla() {
        List<OpcionDTO> opciones = List.of(op("a", false, null, null), op("b", true, null, null),
                op("c", false, null, null));
        assertThatCode(() -> ValidadorPregunta.validar(TipoPregunta.COMPLETACION, opciones)).doesNotThrowAnyException();
    }

    @Test
    void seleccionSinCorrectaEsInvalida() {
        List<OpcionDTO> opciones = List.of(op("a", false, null, null), op("b", false, null, null));
        assertThatThrownBy(() -> ValidadorPregunta.validar(TipoPregunta.SELECCION, opciones))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void seleccionConDosCorrectasEsInvalida() {
        List<OpcionDTO> opciones = List.of(op("a", true, null, null), op("b", true, null, null));
        assertThatThrownBy(() -> ValidadorPregunta.validar(TipoPregunta.SELECCION, opciones))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void conMenosDeDosOpcionesEsInvalida() {
        List<OpcionDTO> opciones = List.of(op("a", true, null, null));
        assertThatThrownBy(() -> ValidadorPregunta.validar(TipoPregunta.SELECCION, opciones))
                .isInstanceOf(BadRequestException.class);
    }

    // ---------- secuencia ----------

    @Test
    void secuenciaConPosicionesDeUnoAEneEsValida() {
        List<OpcionDTO> opciones = List.of(op("pa", null, 2, null), op("so", null, 1, null));
        assertThatCode(() -> ValidadorPregunta.validar(TipoPregunta.SECUENCIA, opciones)).doesNotThrowAnyException();
    }

    @Test
    void secuenciaConPosicionRepetidaEsInvalida() {
        List<OpcionDTO> opciones = List.of(op("a", null, 1, null), op("b", null, 1, null));
        assertThatThrownBy(() -> ValidadorPregunta.validar(TipoPregunta.SECUENCIA, opciones))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void secuenciaConSaltoEsInvalida() {
        List<OpcionDTO> opciones = List.of(op("a", null, 1, null), op("b", null, 3, null));
        assertThatThrownBy(() -> ValidadorPregunta.validar(TipoPregunta.SECUENCIA, opciones))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void secuenciaConOpcionSinPosicionEsInvalida() {
        List<OpcionDTO> opciones = List.of(op("a", null, 1, null), op("b", null, null, null));
        assertThatThrownBy(() -> ValidadorPregunta.validar(TipoPregunta.SECUENCIA, opciones))
                .isInstanceOf(BadRequestException.class);
    }

    // ---------- asociación ----------

    @Test
    void asociacionConParesCompletosEsValida() {
        List<OpcionDTO> opciones = List.of(op("gato", null, null, "A"), op("miau", null, null, "A"),
                op("sol", null, null, "B"), op("brilla", null, null, "B"));
        assertThatCode(() -> ValidadorPregunta.validar(TipoPregunta.ASOCIACION, opciones)).doesNotThrowAnyException();
    }

    @Test
    void asociacionConCantidadImparEsInvalida() {
        List<OpcionDTO> opciones = List.of(op("a", null, null, "A"), op("b", null, null, "A"),
                op("c", null, null, "B"));
        assertThatThrownBy(() -> ValidadorPregunta.validar(TipoPregunta.ASOCIACION, opciones))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void asociacionConUnaClaveTresVecesEsInvalida() {
        List<OpcionDTO> opciones = List.of(op("a", null, null, "A"), op("b", null, null, "A"),
                op("c", null, null, "A"), op("d", null, null, "B"));
        assertThatThrownBy(() -> ValidadorPregunta.validar(TipoPregunta.ASOCIACION, opciones))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void asociacionSinClaveEsInvalida() {
        List<OpcionDTO> opciones = List.of(op("a", null, null, "A"), op("b", null, null, null));
        assertThatThrownBy(() -> ValidadorPregunta.validar(TipoPregunta.ASOCIACION, opciones))
                .isInstanceOf(BadRequestException.class);
    }
}
