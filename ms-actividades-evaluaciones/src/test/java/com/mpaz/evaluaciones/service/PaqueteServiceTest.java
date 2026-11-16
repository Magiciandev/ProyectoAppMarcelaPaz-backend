package com.mpaz.evaluaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mpaz.evaluaciones.dto.PaqueteDTO;
import com.mpaz.evaluaciones.model.ActividadDesafio;
import com.mpaz.evaluaciones.model.ActividadPregunta;
import com.mpaz.evaluaciones.model.Estado;
import com.mpaz.evaluaciones.model.PruebaFinal;
import com.mpaz.evaluaciones.model.PruebaPregunta;
import com.mpaz.evaluaciones.model.Rol;
import com.mpaz.evaluaciones.repository.ActividadPreguntaRepository;
import com.mpaz.evaluaciones.repository.ActividadRepository;
import com.mpaz.evaluaciones.repository.PreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaPreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaRepository;

class PaqueteServiceTest {

    private ActividadRepository actividades;
    private ActividadPreguntaRepository vinculosActividad;
    private PruebaRepository pruebas;
    private PruebaPreguntaRepository vinculosPrueba;
    private PreguntaRepository preguntas;
    private PreguntaService preguntaService;
    private PaqueteService service;

    @BeforeEach
    void preparar() {
        actividades = mock(ActividadRepository.class);
        vinculosActividad = mock(ActividadPreguntaRepository.class);
        pruebas = mock(PruebaRepository.class);
        vinculosPrueba = mock(PruebaPreguntaRepository.class);
        preguntas = mock(PreguntaRepository.class);
        preguntaService = mock(PreguntaService.class);
        service = new PaqueteService(actividades, vinculosActividad, pruebas, vinculosPrueba, preguntas,
                preguntaService);
        when(preguntaService.aDtos(any())).thenReturn(List.of());
    }

    private ActividadDesafio actividad(long id, Estado estado) {
        return new ActividadDesafio(id, 10L, "Actividad " + id, "SELECCION", 1, (int) id, estado.name());
    }

    @Test
    void docenteSoloRecibeLoAutorizadoOPublicado() {
        when(actividades.findByObjetivoAprendizajeIdOrderByOrdenAsc(10L)).thenReturn(List.of(
                actividad(1, Estado.BORRADOR), actividad(2, Estado.AUTORIZADO), actividad(3, Estado.PUBLICADO)));
        when(vinculosActividad.findByActividadIdIn(any())).thenReturn(List.of(
                new ActividadPregunta(1L, 2L, 7L, 1)));
        when(pruebas.findByObjetivoAprendizajeId(10L)).thenReturn(Optional.empty());

        PaqueteDTO paquete = service.paquete(Rol.DOCENTE, 10L);

        assertThat(paquete.getActividades()).extracting(a -> a.getId()).containsExactly(2L, 3L);
        assertThat(paquete.getActividades().get(0).getPreguntas()).hasSize(1);
        assertThat(paquete.getPrueba()).isNull();
    }

    @Test
    void estudianteSoloRecibeLoPublicado() {
        when(actividades.findByObjetivoAprendizajeIdOrderByOrdenAsc(10L)).thenReturn(List.of(
                actividad(2, Estado.AUTORIZADO), actividad(3, Estado.PUBLICADO)));
        when(vinculosActividad.findByActividadIdIn(any())).thenReturn(List.of());
        when(pruebas.findByObjetivoAprendizajeId(10L)).thenReturn(Optional.of(
                new PruebaFinal(5L, 10L, "Prueba", null, 3, Estado.AUTORIZADO.name())));

        PaqueteDTO paquete = service.paquete(Rol.ESTUDIANTE, 10L);

        assertThat(paquete.getActividades()).extracting(a -> a.getId()).containsExactly(3L);
        assertThat(paquete.getPrueba()).isNull();
    }

    @Test
    void utpRecibeLaPruebaConSusPreguntasAunqueEsteEnBorrador() {
        when(actividades.findByObjetivoAprendizajeIdOrderByOrdenAsc(10L)).thenReturn(List.of());
        when(pruebas.findByObjetivoAprendizajeId(10L)).thenReturn(Optional.of(
                new PruebaFinal(5L, 10L, "Prueba", null, 3, Estado.BORRADOR.name())));
        when(vinculosPrueba.findByPruebaIdOrderByOrdenEnPruebaAsc(5L)).thenReturn(List.of(
                new PruebaPregunta(1L, 5L, 7L, 1, BigDecimal.ONE)));

        PaqueteDTO paquete = service.paquete(Rol.UTP, 10L);

        assertThat(paquete.getPrueba()).isNotNull();
        assertThat(paquete.getPrueba().getPreguntas()).hasSize(1);
    }

    @Test
    void unObjetivoSinNadaDevuelveUnPaqueteVacio() {
        when(actividades.findByObjetivoAprendizajeIdOrderByOrdenAsc(99L)).thenReturn(List.of());
        when(pruebas.findByObjetivoAprendizajeId(99L)).thenReturn(Optional.empty());

        PaqueteDTO paquete = service.paquete(Rol.ESTUDIANTE, 99L);

        assertThat(paquete.getActividades()).isEmpty();
        assertThat(paquete.getPrueba()).isNull();
    }
}
