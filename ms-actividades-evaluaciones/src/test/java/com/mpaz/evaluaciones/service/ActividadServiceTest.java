package com.mpaz.evaluaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mpaz.evaluaciones.dto.PreguntaEnActividadDTO;
import com.mpaz.evaluaciones.exception.BadRequestException;
import com.mpaz.evaluaciones.exception.ConflictException;
import com.mpaz.evaluaciones.model.ActividadDesafio;
import com.mpaz.evaluaciones.model.Estado;
import com.mpaz.evaluaciones.model.Pregunta;
import com.mpaz.evaluaciones.repository.ActividadPreguntaRepository;
import com.mpaz.evaluaciones.repository.ActividadRepository;
import com.mpaz.evaluaciones.repository.PreguntaRepository;

class ActividadServiceTest {

    private ActividadRepository actividades;
    private ActividadPreguntaRepository vinculos;
    private PreguntaRepository preguntas;
    private ActividadService service;

    @BeforeEach
    void preparar() {
        actividades = mock(ActividadRepository.class);
        vinculos = mock(ActividadPreguntaRepository.class);
        preguntas = mock(PreguntaRepository.class);
        service = new ActividadService(actividades, vinculos, preguntas);
    }

    private ActividadDesafio actividad(Estado estado) {
        return new ActividadDesafio(1L, 10L, "Actividad", "SELECCION", 1, 1, estado.name());
    }

    private Pregunta pregunta(long id, long objetivoId, String tipo) {
        return new Pregunta(id, objetivoId, tipo, "Enunciado", null);
    }

    @Test
    void reemplazaLasPreguntasValidasYVaciaAntesDeInsertar() {
        when(actividades.findById(1L)).thenReturn(Optional.of(actividad(Estado.BORRADOR)));
        when(preguntas.findById(5L)).thenReturn(Optional.of(pregunta(5, 10, "SELECCION")));

        List<PreguntaEnActividadDTO> r = service.reemplazarPreguntas(1L, List.of(new PreguntaEnActividadDTO(5L, null)));

        assertThat(r).hasSize(1);
        assertThat(r.get(0).getOrden()).isEqualTo(1);
        verify(vinculos).deleteByActividadId(1L);
        verify(vinculos).flush();
        verify(vinculos).saveAll(anyList());
    }

    @Test
    void noAceptaPreguntasDeOtroObjetivo() {
        when(actividades.findById(1L)).thenReturn(Optional.of(actividad(Estado.BORRADOR)));
        when(preguntas.findById(5L)).thenReturn(Optional.of(pregunta(5, 99, "SELECCION")));

        assertThatThrownBy(() -> service.reemplazarPreguntas(1L, List.of(new PreguntaEnActividadDTO(5L, 1))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void noAceptaPreguntasDeOtroTipo() {
        when(actividades.findById(1L)).thenReturn(Optional.of(actividad(Estado.BORRADOR)));
        when(preguntas.findById(5L)).thenReturn(Optional.of(pregunta(5, 10, "SECUENCIA")));

        assertThatThrownBy(() -> service.reemplazarPreguntas(1L, List.of(new PreguntaEnActividadDTO(5L, 1))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void noAceptaPreguntasRepetidas() {
        when(actividades.findById(1L)).thenReturn(Optional.of(actividad(Estado.BORRADOR)));
        when(preguntas.findById(5L)).thenReturn(Optional.of(pregunta(5, 10, "SELECCION")));

        assertThatThrownBy(() -> service.reemplazarPreguntas(1L,
                List.of(new PreguntaEnActividadDTO(5L, 1), new PreguntaEnActividadDTO(5L, 2))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void noSeEditaUnaActividadFueraDeBorrador() {
        when(actividades.findById(1L)).thenReturn(Optional.of(actividad(Estado.PUBLICADO)));

        assertThatThrownBy(() -> service.reemplazarPreguntas(1L, List.of(new PreguntaEnActividadDTO(5L, 1))))
                .isInstanceOf(ConflictException.class);
    }
}
