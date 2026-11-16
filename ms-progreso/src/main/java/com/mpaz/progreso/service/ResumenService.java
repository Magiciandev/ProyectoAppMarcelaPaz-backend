package com.mpaz.progreso.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.progreso.dto.IntentoDTO;
import com.mpaz.progreso.dto.ResumenDTO;
import com.mpaz.progreso.dto.ResumenObjetivoDTO;
import com.mpaz.progreso.dto.RespuestaDTO;
import com.mpaz.progreso.model.Asignacion;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.IntentoPrueba;
import com.mpaz.progreso.model.NivelLogro;
import com.mpaz.progreso.model.Progreso;
import com.mpaz.progreso.model.ReglasProgreso;
import com.mpaz.progreso.model.RespuestaEstudiante;
import com.mpaz.progreso.repository.IntentoRepository;
import com.mpaz.progreso.repository.ProgresoRepository;
import com.mpaz.progreso.repository.RespuestaRepository;

/** Mejor resultado, nivel y avance de un estudiante por objetivo (uso docente y UTP). */
@Service
public class ResumenService {

    private final AsignacionService asignacionService;
    private final IntentoRepository intentoRepository;
    private final ProgresoRepository progresoRepository;
    private final RespuestaRepository respuestaRepository;

    public ResumenService(AsignacionService asignacionService, IntentoRepository intentoRepository,
                          ProgresoRepository progresoRepository, RespuestaRepository respuestaRepository) {
        this.asignacionService = asignacionService;
        this.intentoRepository = intentoRepository;
        this.progresoRepository = progresoRepository;
        this.respuestaRepository = respuestaRepository;
    }

    @Transactional(readOnly = true)
    public ResumenDTO resumen(Estudiante estudiante) {
        List<Asignacion> asignaciones = asignacionService.aplicables(estudiante);
        Map<Long, Progreso> progresos = progresoRepository.findByEstudianteId(estudiante.getUsuarioId()).stream()
                .collect(Collectors.toMap(Progreso::getObjetivoAprendizajeId, p -> p, (a, b) -> a));
        Map<Long, List<IntentoPrueba>> aceptados = intentoRepository
                .findByEstudianteIdAndAceptadoTrueAndArchivadoFalse(estudiante.getUsuarioId()).stream()
                .collect(Collectors.groupingBy(IntentoPrueba::getObjetivoAprendizajeId));

        List<ResumenObjetivoDTO> objetivos = new ArrayList<>();
        List<Integer> mejores = new ArrayList<>();
        for (Asignacion a : asignaciones) {
            Long objetivoId = a.getObjetivoAprendizajeId();
            List<IntentoPrueba> intentos = aceptados.getOrDefault(objetivoId, List.of());
            Integer mejor = intentos.stream().map(IntentoPrueba::getPorcentaje).max(Integer::compare).orElse(null);
            Progreso progreso = progresos.get(objetivoId);
            objetivos.add(new ResumenObjetivoDTO(objetivoId,
                    progreso != null ? progreso.getPorcentajeAvance() : 0,
                    intentos.size(), a.getLimiteIntentos(), mejor,
                    mejor != null ? NivelLogro.de(mejor).name() : null));
            if (mejor != null) {
                mejores.add(mejor);
            }
        }
        Integer promedio = ReglasProgreso.promedio(mejores);
        return new ResumenDTO(estudiante.getUsuarioId(), estudiante.getNombre(), objetivos, promedio,
                promedio != null ? NivelLogro.de(promedio).name() : null);
    }

    /** Intentos no archivados (aceptados y rechazados) con sus respuestas, para que el docente los revise. */
    @Transactional(readOnly = true)
    public List<IntentoDTO> intentos(Estudiante estudiante, Long objetivoId) {
        List<IntentoPrueba> intentos = objetivoId == null
                ? intentoRepository.findByEstudianteIdAndArchivadoFalseOrderByFechaHoraClienteAsc(estudiante.getUsuarioId())
                : intentoRepository.findByEstudianteIdAndObjetivoAprendizajeIdAndArchivadoFalseOrderByFechaHoraClienteAsc(
                        estudiante.getUsuarioId(), objetivoId);
        if (intentos.isEmpty()) {
            return List.of();
        }
        Map<Long, List<RespuestaEstudiante>> respuestas = respuestaRepository
                .findByIntentoIdIn(intentos.stream().map(IntentoPrueba::getId).toList()).stream()
                .collect(Collectors.groupingBy(RespuestaEstudiante::getIntentoId));
        return intentos.stream().map(i -> aDto(i, respuestas.getOrDefault(i.getId(), List.of()))).toList();
    }

    private IntentoDTO aDto(IntentoPrueba i, List<RespuestaEstudiante> respuestas) {
        List<RespuestaDTO> lista = respuestas.stream()
                .sorted(Comparator.comparing(RespuestaEstudiante::getMarcaTiempo))
                .map(r -> new RespuestaDTO(r.getPreguntaId(), r.getRespuesta(), r.isEsCorrecta(), r.getPuntajeObtenido()))
                .toList();
        return new IntentoDTO(i.getId(), i.getUuid(), i.getObjetivoAprendizajeId(), i.getPruebaId(), i.getNumeroIntento(),
                i.getPuntajeObtenido(), i.getPuntajeMaximo(), i.getPorcentaje(), i.getFechaHoraCliente(),
                i.isAceptado(), i.getMotivoRechazo(), lista);
    }

    /** Auxiliar para armar mapas de mejores intentos (lo usa el informe). */
    Map<Long, List<IntentoPrueba>> aceptadosPorObjetivo(Long estudianteId) {
        return new HashMap<>(intentoRepository.findByEstudianteIdAndAceptadoTrueAndArchivadoFalse(estudianteId).stream()
                .collect(Collectors.groupingBy(IntentoPrueba::getObjetivoAprendizajeId)));
    }
}
