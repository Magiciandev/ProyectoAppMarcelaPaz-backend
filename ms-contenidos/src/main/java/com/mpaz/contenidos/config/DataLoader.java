package com.mpaz.contenidos.config;

import java.time.Instant;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mpaz.contenidos.model.Asignatura;
import com.mpaz.contenidos.model.ContenidoDidactico;
import com.mpaz.contenidos.model.Estado;
import com.mpaz.contenidos.model.EstadoUnidadCalculator;
import com.mpaz.contenidos.model.ObjetivoAprendizaje;
import com.mpaz.contenidos.model.Unidad;
import com.mpaz.contenidos.repository.AsignaturaRepository;
import com.mpaz.contenidos.repository.ContenidoRepository;
import com.mpaz.contenidos.repository.ObjetivoRepository;
import com.mpaz.contenidos.repository.UnidadRepository;

/**
 * Datos de prueba (material propio y ficticio). Solo se cargan si no hay asignaturas.
 * Deja objetivos en distintos estados para probar la visibilidad por rol:
 *   PUBLICADO (lo ve todo el mundo), AUTORIZADO (UTP y docente), EN_REVISION y BORRADOR (solo UTP).
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final String NIVEL = "2° Básico";
    private static final String AUTOR = "Equipo MPAZ PRO (material propio)";
    private static final String LICENCIA = "Propio";
    /** Id del usuario UTP sembrado en ms-auth. */
    private static final long UTP_ID = 1L;

    private final AsignaturaRepository asignaturaRepository;
    private final UnidadRepository unidadRepository;
    private final ObjetivoRepository objetivoRepository;
    private final ContenidoRepository contenidoRepository;

    public DataLoader(AsignaturaRepository asignaturaRepository, UnidadRepository unidadRepository,
                      ObjetivoRepository objetivoRepository, ContenidoRepository contenidoRepository) {
        this.asignaturaRepository = asignaturaRepository;
        this.unidadRepository = unidadRepository;
        this.objetivoRepository = objetivoRepository;
        this.contenidoRepository = contenidoRepository;
    }

    @Override
    public void run(String... args) {
        if (asignaturaRepository.count() > 0) {
            return;
        }
        Asignatura lenguaje = asignaturaRepository.save(new Asignatura(null, "Lenguaje y Comunicación", NIVEL));
        Asignatura matematica = asignaturaRepository.save(new Asignatura(null, "Matemática", NIVEL));
        Asignatura ciencias = asignaturaRepository.save(new Asignatura(null, "Ciencias Naturales", NIVEL));
        Asignatura historia = asignaturaRepository.save(
                new Asignatura(null, "Historia, Geografía y Ciencias Sociales", NIVEL));

        // Lenguaje: un objetivo publicado con contenido y otro en borrador
        Unidad u1 = unidad(lenguaje, "Mis primeras lecturas", 1);
        ObjetivoAprendizaje lenPub = objetivo(u1, "LEN-OA01", "Leer palabras y oraciones cortas con fluidez.",
                "Lectura", 1, Estado.PUBLICADO);
        contenido(lenPub, "Leemos sílabas y palabras",
                "Las palabras se forman uniendo sílabas. Por ejemplo: ca-sa se lee casa.",
                "Lee en voz alta cada sílaba y luego une todas.", "ca-sa: casa; so-pa: sopa.", 1);
        contenido(lenPub, "Leemos oraciones",
                "Una oración dice algo completo. Empieza con mayúscula y termina con punto.",
                "Lee la oración y cuenta cuántas palabras tiene.", "El gato duerme.", 2);
        objetivo(u1, "LEN-OA02", "Escribir oraciones simples sobre un tema conocido.", "Escritura", 2,
                Estado.BORRADOR);
        recalcular(u1);

        // Matemática: un objetivo publicado
        Unidad u2 = unidad(matematica, "Números hasta el 100", 1);
        ObjetivoAprendizaje matPub = objetivo(u2, "MAT-OA01", "Contar y comparar números del 0 al 100.",
                "Números y operaciones", 1, Estado.PUBLICADO);
        contenido(matPub, "Contamos de 10 en 10",
                "Podemos contar rápido saltando de 10 en 10: 10, 20, 30, 40...",
                "Cuenta con tus dedos y salta de diez en diez.", "10, 20, 30, 40, 50.", 1);
        recalcular(u2);

        // Ciencias: un objetivo autorizado (el docente lo ve, el estudiante todavía no)
        Unidad u3 = unidad(ciencias, "Los seres vivos", 1);
        ObjetivoAprendizaje cieAut = objetivo(u3, "CIE-OA01", "Reconocer las necesidades de los seres vivos.",
                "Ciencias de la vida", 1, Estado.AUTORIZADO);
        contenido(cieAut, "¿Qué necesitan los seres vivos?",
                "Las plantas y los animales necesitan agua, aire y alimento para vivir.",
                "Observa una planta y di qué necesita para crecer.", "Una planta necesita agua y luz.", 1);
        recalcular(u3);

        // Historia: un objetivo en revisión (solo UTP)
        Unidad u4 = unidad(historia, "Mi comunidad", 1);
        ObjetivoAprendizaje hisRev = objetivo(u4, "HIS-OA01", "Reconocer los lugares importantes de mi comunidad.",
                "Historia y comunidad", 1, Estado.EN_REVISION);
        contenido(hisRev, "Lugares de mi comunidad",
                "En una comunidad hay lugares como la escuela, la plaza y el consultorio.",
                "Dibuja el lugar que más te gusta de tu comunidad.", "La escuela es un lugar para aprender.", 1);
        recalcular(u4);
    }

    private Unidad unidad(Asignatura asignatura, String titulo, int orden) {
        Unidad u = new Unidad(null, asignatura.getId(), titulo, orden, Estado.BORRADOR.name(),
                null, null, UTP_ID, Instant.now());
        return unidadRepository.save(u);
    }

    private ObjetivoAprendizaje objetivo(Unidad unidad, String codigo, String descripcion, String eje,
                                         int orden, Estado estado) {
        ObjetivoAprendizaje o = new ObjetivoAprendizaje(null, unidad.getId(), codigo, descripcion, eje,
                null, orden, estado.name());
        return objetivoRepository.save(o);
    }

    private void contenido(ObjetivoAprendizaje objetivo, String titulo, String explicacion,
                           String instrucciones, String ejemplos, int orden) {
        contenidoRepository.save(new ContenidoDidactico(null, objetivo.getId(), titulo, explicacion,
                instrucciones, ejemplos, null, null, orden, AUTOR, LICENCIA));
    }

    /** Deja la unidad con el estado derivado de sus objetivos. */
    private void recalcular(Unidad unidad) {
        List<Estado> estados = objetivoRepository.findByUnidadIdOrderByOrdenAsc(unidad.getId()).stream()
                .map(o -> Estado.de(o.getEstado()))
                .toList();
        unidad.setEstado(EstadoUnidadCalculator.calcular(estados).name());
        unidadRepository.save(unidad);
    }
}
