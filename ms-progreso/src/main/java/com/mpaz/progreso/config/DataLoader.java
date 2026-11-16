package com.mpaz.progreso.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mpaz.progreso.model.Asignacion;
import com.mpaz.progreso.model.Curso;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.IntentoPrueba;
import com.mpaz.progreso.model.Progreso;
import com.mpaz.progreso.model.RespuestaEstudiante;
import com.mpaz.progreso.repository.AsignacionRepository;
import com.mpaz.progreso.repository.CursoRepository;
import com.mpaz.progreso.repository.EstudianteRepository;
import com.mpaz.progreso.repository.IntentoRepository;
import com.mpaz.progreso.repository.ProgresoRepository;
import com.mpaz.progreso.repository.RespuestaRepository;

/**
 * Datos de prueba (estudiantes ficticios). Solo se cargan si no hay cursos.
 * Asume los ids que siembra ms-auth: docente = 2 y estudiantes = 3 a 10; y los objetivos 1, 3 de ms-contenidos
 * y las pruebas 1, 2 de ms-actividades-evaluaciones. Las fechas de las asignaciones se calculan desde hoy.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final long DOCENTE_ID = 2L;

    private final CursoRepository cursoRepository;
    private final EstudianteRepository estudianteRepository;
    private final AsignacionRepository asignacionRepository;
    private final ProgresoRepository progresoRepository;
    private final IntentoRepository intentoRepository;
    private final RespuestaRepository respuestaRepository;
    private final Reloj reloj;

    public DataLoader(CursoRepository cursoRepository, EstudianteRepository estudianteRepository,
                      AsignacionRepository asignacionRepository, ProgresoRepository progresoRepository,
                      IntentoRepository intentoRepository, RespuestaRepository respuestaRepository, Reloj reloj) {
        this.cursoRepository = cursoRepository;
        this.estudianteRepository = estudianteRepository;
        this.asignacionRepository = asignacionRepository;
        this.progresoRepository = progresoRepository;
        this.intentoRepository = intentoRepository;
        this.respuestaRepository = respuestaRepository;
        this.reloj = reloj;
    }

    @Override
    public void run(String... args) {
        if (cursoRepository.count() > 0) {
            return;
        }
        Instant ahora = reloj.ahora();
        LocalDate hoy = reloj.hoy();

        Curso cursoA = cursoRepository.save(new Curso(null, "2° A", DOCENTE_ID));
        Curso cursoB = cursoRepository.save(new Curso(null, "2° B", DOCENTE_ID));

        Instant haceUnDia = ahora.minus(Duration.ofDays(1));
        Instant haceDiezDias = ahora.minus(Duration.ofDays(10));
        estudianteRepository.saveAll(List.of(
                new Estudiante(3L, "Ana", cursoA.getId(), haceUnDia, haceUnDia),
                new Estudiante(4L, "Beto", cursoA.getId(), haceDiezDias, haceDiezDias),
                new Estudiante(5L, "Carla", cursoA.getId(), null, null),
                new Estudiante(6L, "Gael", cursoA.getId(), null, null),
                new Estudiante(7L, "Diego", cursoB.getId(), null, null),
                new Estudiante(8L, "Eva", cursoB.getId(), null, null),
                new Estudiante(9L, "Isidora", cursoB.getId(), null, null),
                new Estudiante(10L, "Felipe", cursoB.getId(), null, null)));

        // Objetivos 1 y 3 para el 2° A; objetivo 1 para el 2° B. Ventana: de hace 7 días a 60 días más.
        asignar(1L, cursoA.getId(), hoy, ahora);
        asignar(3L, cursoA.getId(), hoy, ahora);
        asignar(1L, cursoB.getId(), hoy, ahora);

        // Ana: objetivo 1 completo y un intento (3 de 4 puntos = 75 %). Beto: objetivo 1 a medias.
        progresoRepository.save(new Progreso(null, 3L, 1L, 2, 4, 0, 100, haceUnDia));
        progresoRepository.save(new Progreso(null, 4L, 1L, 1, 1, 0, 50, haceDiezDias));
        IntentoPrueba intento = intentoRepository.save(new IntentoPrueba(null, "00000000-0000-4000-8000-000000000001",
                3L, 1L, 1L, 1, new BigDecimal("3.00"), new BigDecimal("4.00"), 75, haceUnDia, haceUnDia,
                true, null, false));
        respuesta("00000000-0000-4000-8000-0000000000a1", intento, 1L, "{\"opcionId\":1}", true, "1.00", haceUnDia);
        respuesta("00000000-0000-4000-8000-0000000000a2", intento, 2L, "{\"opcionId\":5}", false, "0.00", haceUnDia);
        respuesta("00000000-0000-4000-8000-0000000000a3", intento, 3L, "{\"orden\":[2,1]}", true, "2.00", haceUnDia);
    }

    private void asignar(long objetivoId, long cursoId, LocalDate hoy, Instant ahora) {
        asignacionRepository.save(new Asignacion(null, objetivoId, cursoId, null, DOCENTE_ID, true,
                hoy.minusDays(7), hoy.plusDays(60), 3, null, null, ahora));
    }

    private void respuesta(String uuid, IntentoPrueba intento, long preguntaId, String respuesta, boolean correcta,
                           String puntaje, Instant momento) {
        respuestaRepository.save(new RespuestaEstudiante(null, uuid, intento.getId(), preguntaId, respuesta, correcta,
                new BigDecimal(puntaje), momento));
    }
}
