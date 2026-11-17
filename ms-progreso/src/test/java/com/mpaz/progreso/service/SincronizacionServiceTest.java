package com.mpaz.progreso.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mpaz.progreso.config.Reloj;
import com.mpaz.progreso.dto.IntentoSync;
import com.mpaz.progreso.dto.ProgresoSync;
import com.mpaz.progreso.dto.RespuestaSync;
import com.mpaz.progreso.dto.ResultadoIntento;
import com.mpaz.progreso.dto.ResultadoProgreso;
import com.mpaz.progreso.dto.SyncRequest;
import com.mpaz.progreso.dto.SyncResponse;
import com.mpaz.progreso.model.Asignacion;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.IntentoPrueba;
import com.mpaz.progreso.model.Progreso;
import com.mpaz.progreso.model.ReinicioProgreso;
import com.mpaz.progreso.repository.EstudianteRepository;
import com.mpaz.progreso.repository.IntentoRepository;
import com.mpaz.progreso.repository.ProgresoRepository;
import com.mpaz.progreso.repository.ReinicioRepository;
import com.mpaz.progreso.repository.RespuestaRepository;

class SincronizacionServiceTest {

    private static final Instant AHORA = Instant.parse("2026-11-16T15:00:00Z");
    private static final long ESTUDIANTE_ID = 3L;
    private static final long OBJETIVO_ID = 3L;

    private EstudianteRepository estudiantes;
    private IntentoRepository intentos;
    private RespuestaRepository respuestas;
    private ProgresoRepository progresos;
    private ReinicioRepository reinicios;
    private AsignacionService asignaciones;
    private SincronizacionService service;
    private Estudiante ana;

    @BeforeEach
    void preparar() {
        estudiantes = mock(EstudianteRepository.class);
        intentos = mock(IntentoRepository.class);
        respuestas = mock(RespuestaRepository.class);
        progresos = mock(ProgresoRepository.class);
        reinicios = mock(ReinicioRepository.class);
        asignaciones = mock(AsignacionService.class);
        Reloj reloj = new Reloj(Clock.fixed(AHORA, ZoneOffset.UTC), "America/Santiago");
        service = new SincronizacionService(estudiantes, intentos, respuestas, progresos, reinicios, asignaciones, reloj);

        ana = new Estudiante(ESTUDIANTE_ID, "Ana", 1L, null, null);
        when(estudiantes.findById(ESTUDIANTE_ID)).thenReturn(Optional.of(ana));
        when(reinicios.findByEstudianteIdOrderByFechaDesc(ESTUDIANTE_ID)).thenReturn(List.of());
        when(intentos.save(any(IntentoPrueba.class))).thenAnswer(inv -> inv.getArgument(0));
        when(progresos.save(any(Progreso.class))).thenAnswer(inv -> inv.getArgument(0));
        conAsignacion(3);
    }

    private void conAsignacion(int limite) {
        Asignacion a = new Asignacion(1L, OBJETIVO_ID, 1L, null, 2L, true, LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 12, 31), limite, null, null, AHORA);
        when(asignaciones.buscar(ana, OBJETIVO_ID)).thenReturn(Optional.of(a));
    }

    private IntentoSync intento(String uuid, String fechaHora, String obtenido) {
        return new IntentoSync(uuid, 5L, OBJETIVO_ID, 1, new BigDecimal(obtenido), new BigDecimal("4"),
                Instant.parse(fechaHora), List.of(new RespuestaSync("r-" + uuid, 10L, "{\"opcionId\":1}", true,
                        BigDecimal.ONE, Instant.parse(fechaHora))));
    }

    private SyncRequest soloIntentos(IntentoSync... lista) {
        return new SyncRequest(AHORA, List.of(lista), List.of());
    }

    private Map<String, ResultadoIntento> porUuid(SyncResponse r) {
        return r.getIntentos().stream().collect(Collectors.toMap(ResultadoIntento::getUuid, x -> x));
    }

    // ---------------------------------------------------------------- intentos

    @Test
    void aceptaUnIntentoValido() {
        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloIntentos(intento("u1", "2026-11-16T14:00:00Z", "3")));

        assertThat(r.getIntentos()).hasSize(1);
        assertThat(r.getIntentos().get(0).getResultado()).isEqualTo("ACEPTADO");
        verify(respuestas).save(any());
    }

    @Test
    void guardaElPorcentajeCalculado() {
        service.sincronizar(ESTUDIANTE_ID, soloIntentos(intento("u1", "2026-11-16T14:00:00Z", "3")));

        org.mockito.ArgumentCaptor<IntentoPrueba> captor = org.mockito.ArgumentCaptor.forClass(IntentoPrueba.class);
        verify(intentos).save(captor.capture());
        assertThat(captor.getValue().getPorcentaje()).isEqualTo(75);
        assertThat(captor.getValue().isAceptado()).isTrue();
    }

    @Test
    void unIntentoYaRecibidoSeTrataComoDuplicadoYNoSeGuardaDeNuevo() {
        IntentoPrueba previo = new IntentoPrueba(1L, "u1", ESTUDIANTE_ID, 5L, OBJETIVO_ID, 1, BigDecimal.ONE,
                BigDecimal.ONE, 100, AHORA, AHORA, true, null, false);
        when(intentos.findByUuid("u1")).thenReturn(Optional.of(previo));

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloIntentos(intento("u1", "2026-11-16T14:00:00Z", "3")));

        assertThat(r.getIntentos().get(0).getResultado()).isEqualTo("DUPLICADO");
        verify(intentos, never()).save(any());
    }

    @Test
    void unIntentoRechazadoAntesSigueRechazadoConElMismoMotivo() {
        IntentoPrueba previo = new IntentoPrueba(1L, "u1", ESTUDIANTE_ID, 5L, OBJETIVO_ID, 1, BigDecimal.ONE,
                BigDecimal.ONE, 100, AHORA, AHORA, false, "LIMITE_INTENTOS", false);
        when(intentos.findByUuid("u1")).thenReturn(Optional.of(previo));

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloIntentos(intento("u1", "2026-11-16T14:00:00Z", "3")));

        assertThat(r.getIntentos().get(0).getResultado()).isEqualTo("RECHAZADO");
        assertThat(r.getIntentos().get(0).getMotivo()).isEqualTo("LIMITE_INTENTOS");
    }

    @Test
    void sinAsignacionSeRechaza() {
        when(asignaciones.buscar(ana, OBJETIVO_ID)).thenReturn(Optional.empty());

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloIntentos(intento("u1", "2026-11-16T14:00:00Z", "3")));

        assertThat(r.getIntentos().get(0).getResultado()).isEqualTo("RECHAZADO");
        assertThat(r.getIntentos().get(0).getMotivo()).isEqualTo("SIN_ASIGNACION");
    }

    @Test
    void fueraDeLaVentanaSeRechazaSegunLaFechaDelDispositivo() {
        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloIntentos(intento("u1", "2020-01-01T12:00:00Z", "3")));

        assertThat(r.getIntentos().get(0).getMotivo()).isEqualTo("FUERA_DE_FECHA");
    }

    @Test
    void unIntentoAnteriorAlReinicioSeRechaza() {
        ReinicioProgreso reinicio = new ReinicioProgreso(1L, ESTUDIANTE_ID, OBJETIVO_ID, 2L,
                Instant.parse("2026-11-16T14:30:00Z"), null);
        when(reinicios.findByEstudianteIdOrderByFechaDesc(ESTUDIANTE_ID)).thenReturn(List.of(reinicio));

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloIntentos(intento("u1", "2026-11-16T14:00:00Z", "3")));

        assertThat(r.getIntentos().get(0).getMotivo()).isEqualTo("ANTERIOR_AL_REINICIO");
        assertThat(r.getUltimoReinicio()).isEqualTo(Instant.parse("2026-11-16T14:30:00Z"));
    }

    @Test
    void conElLimiteAlcanzadoSeRechaza() {
        when(intentos.countByEstudianteIdAndObjetivoAprendizajeIdAndAceptadoTrueAndArchivadoFalse(ESTUDIANTE_ID, OBJETIVO_ID))
                .thenReturn(3L);

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloIntentos(intento("u1", "2026-11-16T14:00:00Z", "3")));

        assertThat(r.getIntentos().get(0).getMotivo()).isEqualTo("LIMITE_INTENTOS");
        verify(respuestas, never()).save(any());
    }

    @Test
    void sonValidosLosPrimerosNSegunLaFechaDelDispositivoAunqueLleguenDesordenados() {
        conAsignacion(2);
        when(intentos.countByEstudianteIdAndObjetivoAprendizajeIdAndAceptadoTrueAndArchivadoFalse(ESTUDIANTE_ID, OBJETIVO_ID))
                .thenReturn(0L, 1L, 2L);

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloIntentos(
                intento("u3", "2026-11-16T14:03:00Z", "4"),
                intento("u1", "2026-11-16T14:01:00Z", "2"),
                intento("u2", "2026-11-16T14:02:00Z", "3")));

        Map<String, ResultadoIntento> resultados = porUuid(r);
        assertThat(resultados.get("u1").getResultado()).isEqualTo("ACEPTADO");
        assertThat(resultados.get("u2").getResultado()).isEqualTo("ACEPTADO");
        assertThat(resultados.get("u3").getResultado()).isEqualTo("RECHAZADO");
        assertThat(resultados.get("u3").getMotivo()).isEqualTo("LIMITE_INTENTOS");
    }

    @Test
    void unPuntajeMayorAlMaximoEsInvalidoYNoSeGuarda() {
        IntentoSync malo = new IntentoSync("u1", 5L, OBJETIVO_ID, 1, new BigDecimal("5"), new BigDecimal("4"),
                Instant.parse("2026-11-16T14:00:00Z"), List.of());

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloIntentos(malo));

        assertThat(r.getIntentos().get(0).getMotivo()).isEqualTo("DATOS_INVALIDOS");
        verify(intentos, never()).save(any());
    }

    // ---------------------------------------------------------------- progreso

    private SyncRequest soloProgreso(ProgresoSync... lista) {
        return new SyncRequest(AHORA, List.of(), List.of(lista));
    }

    private ProgresoSync progreso(String marcaTiempo, int porcentaje) {
        return new ProgresoSync(OBJETIVO_ID, 2, 1, 0, porcentaje, Instant.parse(marcaTiempo));
    }

    @Test
    void aceptaUnProgresoNuevo() {
        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloProgreso(progreso("2026-11-16T14:00:00Z", 75)));

        ResultadoProgreso resultado = r.getProgreso().get(0);
        assertThat(resultado.getResultado()).isEqualTo("ACEPTADO");
        verify(progresos).save(any(Progreso.class));
    }

    @Test
    void unProgresoMasAntiguoQueElGuardadoSeIgnoraComoDuplicado() {
        Progreso existente = new Progreso(1L, ESTUDIANTE_ID, OBJETIVO_ID, 2, 1, 0, 80, Instant.parse("2026-11-16T14:30:00Z"));
        when(progresos.findByEstudianteIdAndObjetivoAprendizajeId(ESTUDIANTE_ID, OBJETIVO_ID))
                .thenReturn(Optional.of(existente));

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloProgreso(progreso("2026-11-16T14:00:00Z", 50)));

        assertThat(r.getProgreso().get(0).getResultado()).isEqualTo("DUPLICADO");
        assertThat(existente.getPorcentajeAvance()).isEqualTo(80);
    }

    @Test
    void unProgresoMasNuevoActualizaLaFila() {
        Progreso existente = new Progreso(1L, ESTUDIANTE_ID, OBJETIVO_ID, 1, 0, 0, 30, Instant.parse("2026-11-16T13:00:00Z"));
        when(progresos.findByEstudianteIdAndObjetivoAprendizajeId(ESTUDIANTE_ID, OBJETIVO_ID))
                .thenReturn(Optional.of(existente));

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloProgreso(progreso("2026-11-16T14:00:00Z", 90)));

        assertThat(r.getProgreso().get(0).getResultado()).isEqualTo("ACEPTADO");
        assertThat(existente.getPorcentajeAvance()).isEqualTo(90);
    }

    @Test
    void unProgresoFueraDeRangoEsInvalido() {
        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloProgreso(progreso("2026-11-16T14:00:00Z", 140)));

        assertThat(r.getProgreso().get(0).getMotivo()).isEqualTo("DATOS_INVALIDOS");
    }

    @Test
    void unProgresoConLaMismaMarcaTiempoSeTrataComoDuplicadoYNoSeGuarda() {
        // Mismo marcaTiempo que el enviado en la primera petición
        Instant marca = Instant.parse("2026-11-16T14:00:00.123456Z");
        Progreso existente = new Progreso(1L, ESTUDIANTE_ID, OBJETIVO_ID, 2, 1, 0, 80, marca);
        when(progresos.findByEstudianteIdAndObjetivoAprendizajeId(ESTUDIANTE_ID, OBJETIVO_ID))
                .thenReturn(Optional.of(existente));

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloProgreso(new ProgresoSync(OBJETIVO_ID, 2, 1, 0, 50, marca)));

        assertThat(r.getProgreso().get(0).getResultado()).isEqualTo("DUPLICADO");
        assertThat(existente.getPorcentajeAvance()).isEqualTo(80); // No se sobrescribe
        verify(progresos, never()).save(any());
    }

    @Test
    void unProgresoSinAsignacionSeRechaza() {
        when(asignaciones.buscar(ana, OBJETIVO_ID)).thenReturn(Optional.empty());

        SyncResponse r = service.sincronizar(ESTUDIANTE_ID, soloProgreso(progreso("2026-11-16T14:00:00Z", 50)));

        assertThat(r.getProgreso().get(0).getMotivo()).isEqualTo("SIN_ASIGNACION");
    }

    // ---------------------------------------------------------------- conexión

    @Test
    void sincronizarActualizaLaUltimaConexionYLaUltimaSincronizacion() {
        service.sincronizar(ESTUDIANTE_ID, new SyncRequest(AHORA, List.of(), List.of()));

        assertThat(ana.getUltimaConexion()).isEqualTo(AHORA);
        assertThat(ana.getUltimaSincronizacion()).isEqualTo(AHORA);
        verify(estudiantes).save(ana);
    }
}
