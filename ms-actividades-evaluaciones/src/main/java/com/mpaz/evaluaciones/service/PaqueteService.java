package com.mpaz.evaluaciones.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.evaluaciones.dto.ActividadDTO;
import com.mpaz.evaluaciones.dto.PaqueteDTO;
import com.mpaz.evaluaciones.dto.PreguntaEnActividadDTO;
import com.mpaz.evaluaciones.dto.PreguntaEnPruebaDTO;
import com.mpaz.evaluaciones.dto.PruebaDTO;
import com.mpaz.evaluaciones.model.ActividadDesafio;
import com.mpaz.evaluaciones.model.ActividadPregunta;
import com.mpaz.evaluaciones.model.Estado;
import com.mpaz.evaluaciones.model.Pregunta;
import com.mpaz.evaluaciones.model.PruebaFinal;
import com.mpaz.evaluaciones.model.Rol;
import com.mpaz.evaluaciones.repository.ActividadPreguntaRepository;
import com.mpaz.evaluaciones.repository.ActividadRepository;
import com.mpaz.evaluaciones.repository.PreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaPreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaRepository;

/**
 * Paquete de evaluación de un objetivo, pensado para la caché de la app: actividades, preguntas con sus
 * opciones (incluye el solucionario, para corregir sin red) y la prueba final. Solo trae lo que el rol puede ver.
 * Si el objetivo no tiene nada visible, responde un paquete vacío (este servicio no conoce los objetivos).
 */
@Service
public class PaqueteService {

    private final ActividadRepository actividadRepository;
    private final ActividadPreguntaRepository actividadPreguntaRepository;
    private final PruebaRepository pruebaRepository;
    private final PruebaPreguntaRepository pruebaPreguntaRepository;
    private final PreguntaRepository preguntaRepository;
    private final PreguntaService preguntaService;

    public PaqueteService(ActividadRepository actividadRepository,
                          ActividadPreguntaRepository actividadPreguntaRepository,
                          PruebaRepository pruebaRepository, PruebaPreguntaRepository pruebaPreguntaRepository,
                          PreguntaRepository preguntaRepository, PreguntaService preguntaService) {
        this.actividadRepository = actividadRepository;
        this.actividadPreguntaRepository = actividadPreguntaRepository;
        this.pruebaRepository = pruebaRepository;
        this.pruebaPreguntaRepository = pruebaPreguntaRepository;
        this.preguntaRepository = preguntaRepository;
        this.preguntaService = preguntaService;
    }

    @Transactional(readOnly = true)
    public PaqueteDTO paquete(Rol rol, Long objetivoId) {
        List<ActividadDesafio> actividades = actividadRepository
                .findByObjetivoAprendizajeIdOrderByOrdenAsc(objetivoId).stream()
                .filter(a -> rol.vee(Estado.de(a.getEstado())))
                .toList();

        Set<Long> preguntaIds = new LinkedHashSet<>();
        Map<Long, List<PreguntaEnActividadDTO>> vinculos = new HashMap<>();
        if (!actividades.isEmpty()) {
            List<Long> actividadIds = actividades.stream().map(ActividadDesafio::getId).toList();
            for (ActividadPregunta ap : actividadPreguntaRepository.findByActividadIdIn(actividadIds)) {
                vinculos.computeIfAbsent(ap.getActividadId(), k -> new ArrayList<>())
                        .add(new PreguntaEnActividadDTO(ap.getPreguntaId(), ap.getOrden()));
                preguntaIds.add(ap.getPreguntaId());
            }
            vinculos.values().forEach(lista -> lista.sort(Comparator.comparing(PreguntaEnActividadDTO::getOrden)));
        }
        List<ActividadDTO> actividadesDto = actividades.stream()
                .map(a -> ActividadDTO.fromModel(a, vinculos.getOrDefault(a.getId(), List.of())))
                .toList();

        PruebaDTO pruebaDto = null;
        Optional<PruebaFinal> prueba = pruebaRepository.findByObjetivoAprendizajeId(objetivoId)
                .filter(p -> rol.vee(Estado.de(p.getEstado())));
        if (prueba.isPresent()) {
            List<PreguntaEnPruebaDTO> enPrueba = pruebaPreguntaRepository
                    .findByPruebaIdOrderByOrdenEnPruebaAsc(prueba.get().getId()).stream()
                    .map(pp -> new PreguntaEnPruebaDTO(pp.getPreguntaId(), pp.getOrdenEnPrueba(), pp.getPuntaje()))
                    .toList();
            enPrueba.forEach(x -> preguntaIds.add(x.getPreguntaId()));
            pruebaDto = PruebaDTO.fromModel(prueba.get(), enPrueba);
        }

        List<Pregunta> preguntas = preguntaIds.isEmpty()
                ? List.of()
                : preguntaRepository.findByIdIn(preguntaIds).stream()
                        .sorted(Comparator.comparing(Pregunta::getId))
                        .toList();
        return new PaqueteDTO(objetivoId, actividadesDto, preguntaService.aDtos(preguntas), pruebaDto);
    }
}
