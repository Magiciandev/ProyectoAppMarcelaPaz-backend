package com.mpaz.evaluaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mpaz.evaluaciones.dto.PreguntaEnPruebaDTO;
import com.mpaz.evaluaciones.dto.PruebaRequest;
import com.mpaz.evaluaciones.exception.BadRequestException;
import com.mpaz.evaluaciones.exception.ConflictException;
import com.mpaz.evaluaciones.exception.ResourceNotFoundException;
import com.mpaz.evaluaciones.model.Estado;
import com.mpaz.evaluaciones.model.Pregunta;
import com.mpaz.evaluaciones.model.PruebaFinal;
import com.mpaz.evaluaciones.model.Rol;
import com.mpaz.evaluaciones.repository.PreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaPreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaRepository;

class PruebaServiceTest {

    private PruebaRepository pruebas;
    private PruebaPreguntaRepository vinculos;
    private PreguntaRepository preguntas;
    private PruebaService service;

    @BeforeEach
    void preparar() {
        pruebas = mock(PruebaRepository.class);
        vinculos = mock(PruebaPreguntaRepository.class);
        preguntas = mock(PreguntaRepository.class);
        service = new PruebaService(pruebas, vinculos, preguntas);
    }

    private PruebaFinal prueba(Estado estado) {
        return new PruebaFinal(5L, 10L, "Prueba final", null, 3, estado.name());
    }

    @Test
    void noSePuedeCrearUnaSegundaPruebaParaElMismoObjetivo() {
        when(pruebas.findByObjetivoAprendizajeId(10L)).thenReturn(Optional.of(prueba(Estado.BORRADOR)));

        assertThatThrownBy(() -> service.crear(new PruebaRequest(10L, "Otra", null, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void docenteNoVeUnaPruebaEnBorrador() {
        when(pruebas.findByObjetivoAprendizajeId(10L)).thenReturn(Optional.of(prueba(Estado.BORRADOR)));

        assertThatThrownBy(() -> service.obtenerPorObjetivo(Rol.DOCENTE, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void estudianteVeUnaPruebaPublicada() {
        when(pruebas.findByObjetivoAprendizajeId(10L)).thenReturn(Optional.of(prueba(Estado.PUBLICADO)));
        when(vinculos.findByPruebaIdOrderByOrdenEnPruebaAsc(5L)).thenReturn(List.of());

        assertThat(service.obtenerPorObjetivo(Rol.ESTUDIANTE, 10L).getId()).isEqualTo(5L);
    }

    @Test
    void elPuntajeDebeSerMayorQueCero() {
        when(pruebas.findById(5L)).thenReturn(Optional.of(prueba(Estado.BORRADOR)));
        when(preguntas.findById(7L)).thenReturn(Optional.of(new Pregunta(7L, 10L, "SELECCION", "E", null)));

        assertThatThrownBy(() -> service.reemplazarPreguntas(5L,
                List.of(new PreguntaEnPruebaDTO(7L, 1, BigDecimal.ZERO))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void noAceptaPreguntasDeOtroObjetivo() {
        when(pruebas.findById(5L)).thenReturn(Optional.of(prueba(Estado.BORRADOR)));
        when(preguntas.findById(7L)).thenReturn(Optional.of(new Pregunta(7L, 99L, "SELECCION", "E", null)));

        assertThatThrownBy(() -> service.reemplazarPreguntas(5L,
                List.of(new PreguntaEnPruebaDTO(7L, 1, BigDecimal.ONE))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void noSeEditaUnaPruebaFueraDeBorrador() {
        when(pruebas.findById(5L)).thenReturn(Optional.of(prueba(Estado.AUTORIZADO)));

        assertThatThrownBy(() -> service.reemplazarPreguntas(5L,
                List.of(new PreguntaEnPruebaDTO(7L, 1, BigDecimal.ONE))))
                .isInstanceOf(ConflictException.class);
    }
}
