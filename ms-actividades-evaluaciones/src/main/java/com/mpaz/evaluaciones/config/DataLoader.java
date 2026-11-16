package com.mpaz.evaluaciones.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mpaz.evaluaciones.model.ActividadDesafio;
import com.mpaz.evaluaciones.model.ActividadPregunta;
import com.mpaz.evaluaciones.model.Estado;
import com.mpaz.evaluaciones.model.Opcion;
import com.mpaz.evaluaciones.model.Pregunta;
import com.mpaz.evaluaciones.model.PruebaFinal;
import com.mpaz.evaluaciones.model.PruebaPregunta;
import com.mpaz.evaluaciones.model.TipoPregunta;
import com.mpaz.evaluaciones.repository.ActividadPreguntaRepository;
import com.mpaz.evaluaciones.repository.ActividadRepository;
import com.mpaz.evaluaciones.repository.OpcionRepository;
import com.mpaz.evaluaciones.repository.PreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaPreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaRepository;

/**
 * Datos de prueba (preguntas propias y ficticias). Solo se cargan si no hay preguntas.
 * Asume los ids de objetivo sembrados por ms-contenidos:
 *   1 = LEN-OA01 (PUBLICADO), 3 = MAT-OA01 (PUBLICADO), 4 = CIE-OA01 (AUTORIZADO), 5 = HIS-OA01 (EN_REVISION).
 * El objetivo 2 (LEN-OA02, BORRADOR) queda sin actividades ni prueba.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final PreguntaRepository preguntaRepository;
    private final OpcionRepository opcionRepository;
    private final ActividadRepository actividadRepository;
    private final ActividadPreguntaRepository actividadPreguntaRepository;
    private final PruebaRepository pruebaRepository;
    private final PruebaPreguntaRepository pruebaPreguntaRepository;

    public DataLoader(PreguntaRepository preguntaRepository, OpcionRepository opcionRepository,
                      ActividadRepository actividadRepository,
                      ActividadPreguntaRepository actividadPreguntaRepository,
                      PruebaRepository pruebaRepository, PruebaPreguntaRepository pruebaPreguntaRepository) {
        this.preguntaRepository = preguntaRepository;
        this.opcionRepository = opcionRepository;
        this.actividadRepository = actividadRepository;
        this.actividadPreguntaRepository = actividadPreguntaRepository;
        this.pruebaRepository = pruebaRepository;
        this.pruebaPreguntaRepository = pruebaPreguntaRepository;
    }

    @Override
    public void run(String... args) {
        if (preguntaRepository.count() > 0) {
            return;
        }

        // ---------------- Objetivo 1 (Lenguaje, PUBLICADO): los cuatro tipos ----------------
        Pregunta p1 = pregunta(1, TipoPregunta.SELECCION, "¿Qué palabra se forma con las sílabas ca-sa?",
                "Al unir ca y sa se lee casa.");
        opcion(p1, "casa", true, null, null, 1);
        opcion(p1, "saca", false, null, null, 2);
        opcion(p1, "cosa", false, null, null, 3);

        Pregunta p2 = pregunta(1, TipoPregunta.COMPLETACION, "El gato ___ en el sillón.",
                "Con un gato usamos «duerme».");
        opcion(p2, "duerme", true, null, null, 1);
        opcion(p2, "duermen", false, null, null, 2);
        opcion(p2, "dormir", false, null, null, 3);

        Pregunta p3 = pregunta(1, TipoPregunta.SECUENCIA, "Ordena las sílabas para formar la palabra sopa.",
                "Primero va so y después pa.");
        opcion(p3, "pa", false, 2, null, 1);   // se muestra primero, pero va en la posición 2
        opcion(p3, "so", false, 1, null, 2);

        Pregunta p4 = pregunta(1, TipoPregunta.ASOCIACION, "Une cada palabra con su pista.",
                "El gato dice miau y el sol brilla de día.");
        opcion(p4, "gato", false, null, "A", 1);
        opcion(p4, "Animal que dice miau", false, null, "A", 2);
        opcion(p4, "sol", false, null, "B", 3);
        opcion(p4, "Brilla de día", false, null, "B", 4);

        actividad(1, "Elige la palabra correcta", TipoPregunta.SELECCION, 1, 1, Estado.PUBLICADO, List.of(p1));
        actividad(1, "Completa la oración", TipoPregunta.COMPLETACION, 1, 2, Estado.PUBLICADO, List.of(p2));
        actividad(1, "Ordena las sílabas", TipoPregunta.SECUENCIA, 2, 3, Estado.PUBLICADO, List.of(p3));
        actividad(1, "Une las parejas", TipoPregunta.ASOCIACION, 2, 4, Estado.PUBLICADO, List.of(p4));
        prueba(1, "Prueba final: primeras lecturas", Estado.PUBLICADO, List.of(p1, p2, p3), List.of(1, 1, 2));

        // ---------------- Objetivo 3 (Matemática, PUBLICADO) ----------------
        Pregunta p5 = pregunta(3, TipoPregunta.SELECCION, "¿Qué número sigue? 10, 20, 30, ...",
                "Se cuenta de 10 en 10.");
        opcion(p5, "40", true, null, null, 1);
        opcion(p5, "50", false, null, null, 2);
        opcion(p5, "35", false, null, null, 3);

        Pregunta p6 = pregunta(3, TipoPregunta.SELECCION, "¿Cuál número es el mayor?",
                "57 tiene más decenas que 48 y 39.");
        opcion(p6, "57", true, null, null, 1);
        opcion(p6, "48", false, null, null, 2);
        opcion(p6, "39", false, null, null, 3);

        actividad(3, "Contamos de 10 en 10", TipoPregunta.SELECCION, 1, 1, Estado.PUBLICADO, List.of(p5));
        prueba(3, "Prueba final: números hasta el 100", Estado.PUBLICADO, List.of(p5, p6), List.of(1, 1));

        // ---------------- Objetivo 4 (Ciencias, AUTORIZADO) ----------------
        Pregunta p7 = pregunta(4, TipoPregunta.SELECCION, "¿Qué necesita una planta para crecer?",
                "Las plantas necesitan agua y luz.");
        opcion(p7, "Agua y luz", true, null, null, 1);
        opcion(p7, "Solo arena", false, null, null, 2);
        opcion(p7, "Nada", false, null, null, 3);

        actividad(4, "Cuidamos las plantas", TipoPregunta.SELECCION, 1, 1, Estado.AUTORIZADO, List.of(p7));
        prueba(4, "Prueba final: los seres vivos", Estado.AUTORIZADO, List.of(p7), List.of(1));

        // ---------------- Objetivo 5 (Historia, EN_REVISION): solo prueba ----------------
        Pregunta p8 = pregunta(5, TipoPregunta.SELECCION, "¿Dónde aprendemos a leer y escribir?",
                "En la escuela aprendemos muchas cosas.");
        opcion(p8, "En la escuela", true, null, null, 1);
        opcion(p8, "En el cielo", false, null, null, 2);
        opcion(p8, "En el mar", false, null, null, 3);

        prueba(5, "Prueba final: mi comunidad", Estado.EN_REVISION, List.of(p8), List.of(1));
    }

    private Pregunta pregunta(long objetivoId, TipoPregunta tipo, String enunciado, String explicacion) {
        return preguntaRepository.save(new Pregunta(null, objetivoId, tipo.name(), enunciado, explicacion));
    }

    private void opcion(Pregunta pregunta, String texto, boolean correcta, Integer posicion, String par, int orden) {
        opcionRepository.save(new Opcion(null, pregunta.getId(), texto, correcta, posicion, par, orden));
    }

    private void actividad(long objetivoId, String titulo, TipoPregunta tipo, int dificultad, int orden,
                           Estado estado, List<Pregunta> preguntas) {
        ActividadDesafio a = actividadRepository.save(new ActividadDesafio(null, objetivoId, titulo, tipo.name(),
                dificultad, orden, estado.name()));
        int posicion = 1;
        for (Pregunta p : preguntas) {
            actividadPreguntaRepository.save(new ActividadPregunta(null, a.getId(), p.getId(), posicion++));
        }
    }

    private void prueba(long objetivoId, String titulo, Estado estado, List<Pregunta> preguntas, List<Integer> puntajes) {
        PruebaFinal prueba = pruebaRepository.save(new PruebaFinal(null, objetivoId, titulo,
                "Cada pregunta vale su puntaje; el resultado se expresa en porcentaje.", 3, estado.name()));
        for (int i = 0; i < preguntas.size(); i++) {
            pruebaPreguntaRepository.save(new PruebaPregunta(null, prueba.getId(), preguntas.get(i).getId(),
                    i + 1, BigDecimal.valueOf(puntajes.get(i))));
        }
    }
}
