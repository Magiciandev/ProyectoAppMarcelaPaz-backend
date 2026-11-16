package com.mpaz.progreso.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mpaz.progreso.cliente.ContenidosClient;
import com.mpaz.progreso.config.Reloj;
import com.mpaz.progreso.dto.AsignacionDTO;
import com.mpaz.progreso.dto.AsignacionRequest;
import com.mpaz.progreso.exception.BadRequestException;
import com.mpaz.progreso.exception.ConflictException;
import com.mpaz.progreso.exception.ForbiddenException;
import com.mpaz.progreso.exception.ResourceNotFoundException;
import com.mpaz.progreso.model.Asignacion;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.Rol;
import com.mpaz.progreso.repository.AsignacionRepository;
import com.mpaz.progreso.repository.EstudianteRepository;

class AsignacionServiceTest {

    private static final String TOKEN = "Bearer abc";

    private AsignacionRepository asignaciones;
    private EstudianteRepository estudiantes;
    private AccesoService acceso;
    private ContenidosClient contenidos;
    private AsignacionService service;

    @BeforeEach
    void preparar() {
        asignaciones = mock(AsignacionRepository.class);
        estudiantes = mock(EstudianteRepository.class);
        acceso = mock(AccesoService.class);
        contenidos = mock(ContenidosClient.class);
        Reloj reloj = new Reloj(Clock.fixed(Instant.parse("2026-11-16T15:00:00Z"), ZoneOffset.UTC), "America/Santiago");
        service = new AsignacionService(asignaciones, estudiantes, acceso, contenidos, reloj);
        when(asignaciones.save(any(Asignacion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(contenidos.objetivoVisible(7L, TOKEN)).thenReturn(true);
    }

    private AsignacionRequest pedidoParaCurso() {
        return new AsignacionRequest(7L, 1L, null, LocalDate.of(2026, 11, 16), LocalDate.of(2026, 12, 18),
                3, null, null, null);
    }

    @Test
    void creaUnaAsignacionParaUnCurso() {
        AsignacionDTO dto = service.crear(2L, pedidoParaCurso(), TOKEN);

        assertThat(dto.getCursoId()).isEqualTo(1L);
        assertThat(dto.getLimiteIntentos()).isEqualTo(3);
        assertThat(dto.isDisponible()).isTrue();
        assertThat(dto.getDocenteId()).isEqualTo(2L);
    }

    @Test
    void sinLimiteUsaTresIntentos() {
        AsignacionRequest r = pedidoParaCurso();
        r.setLimiteIntentos(null);

        assertThat(service.crear(2L, r, TOKEN).getLimiteIntentos()).isEqualTo(3);
    }

    @Test
    void exigeCursoOEstudianteperoNoAmbos() {
        AsignacionRequest ambos = pedidoParaCurso();
        ambos.setEstudianteId(3L);
        AsignacionRequest ninguno = pedidoParaCurso();
        ninguno.setCursoId(null);

        assertThatThrownBy(() -> service.crear(2L, ambos, TOKEN)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.crear(2L, ninguno, TOKEN)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void lasFechasInvertidasSonInvalidas() {
        AsignacionRequest r = pedidoParaCurso();
        r.setFechaDesde(LocalDate.of(2026, 12, 31));

        assertThatThrownBy(() -> service.crear(2L, r, TOKEN)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void noSePuedeAsignarUnObjetivoQueElDocenteNoVe() {
        when(contenidos.objetivoVisible(7L, TOKEN)).thenReturn(false);

        assertThatThrownBy(() -> service.crear(2L, pedidoParaCurso(), TOKEN))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(asignaciones, never()).save(any());
    }

    @Test
    void noSePuedeAsignarDosVecesElMismoObjetivoAlMismoCurso() {
        when(asignaciones.findByObjetivoAprendizajeIdAndCursoId(7L, 1L))
                .thenReturn(Optional.of(new Asignacion()));

        assertThatThrownBy(() -> service.crear(2L, pedidoParaCurso(), TOKEN)).isInstanceOf(ConflictException.class);
    }

    @Test
    void unDocenteNoAsignaEnElCursoDeOtro() {
        doThrow(new ForbiddenException("Ese curso no es tuyo")).when(acceso).cursoPermitido(1L, Rol.DOCENTE, 2L);

        assertThatThrownBy(() -> service.crear(2L, pedidoParaCurso(), TOKEN)).isInstanceOf(ForbiddenException.class);
        verify(contenidos, never()).objetivoVisible(any(), anyString());
    }

    @Test
    void laAsignacionIndividualPrevaleceSobreLaDelCurso() {
        Estudiante ana = new Estudiante(3L, "Ana", 1L, null, null);
        Asignacion delCurso = new Asignacion(1L, 7L, 1L, null, 2L, true, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 12, 31), 3, null, null, Instant.now());
        Asignacion individual = new Asignacion(2L, 7L, null, 3L, 2L, true, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 12, 31), 5, null, null, Instant.now());
        when(asignaciones.findByCursoIdAndDisponibleTrue(1L)).thenReturn(List.of(delCurso));
        when(asignaciones.findByEstudianteIdAndDisponibleTrue(3L)).thenReturn(List.of(individual));

        List<Asignacion> aplicables = service.aplicables(ana);

        assertThat(aplicables).hasSize(1);
        assertThat(aplicables.get(0).getLimiteIntentos()).isEqualTo(5);
    }
}
