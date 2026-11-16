package com.mpaz.evaluaciones.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.evaluaciones.dto.ActividadDTO;
import com.mpaz.evaluaciones.dto.ActividadRequest;
import com.mpaz.evaluaciones.dto.PreguntaEnActividadDTO;
import com.mpaz.evaluaciones.exception.BadRequestException;
import com.mpaz.evaluaciones.exception.ConflictException;
import com.mpaz.evaluaciones.exception.ResourceNotFoundException;
import com.mpaz.evaluaciones.model.ActividadDesafio;
import com.mpaz.evaluaciones.model.ActividadPregunta;
import com.mpaz.evaluaciones.model.Estado;
import com.mpaz.evaluaciones.model.Pregunta;
import com.mpaz.evaluaciones.model.Rol;
import com.mpaz.evaluaciones.model.TipoPregunta;
import com.mpaz.evaluaciones.repository.ActividadPreguntaRepository;
import com.mpaz.evaluaciones.repository.ActividadRepository;
import com.mpaz.evaluaciones.repository.PreguntaRepository;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final ActividadPreguntaRepository actividadPreguntaRepository;
    private final PreguntaRepository preguntaRepository;

    public ActividadService(ActividadRepository actividadRepository,
                            ActividadPreguntaRepository actividadPreguntaRepository,
                            PreguntaRepository preguntaRepository) {
        this.actividadRepository = actividadRepository;
        this.actividadPreguntaRepository = actividadPreguntaRepository;
        this.preguntaRepository = preguntaRepository;
    }

    @Transactional(readOnly = true)
    public List<ActividadDTO> listar(Rol rol, Long objetivoId) {
        return actividadRepository.findByObjetivoAprendizajeIdOrderByOrdenAsc(objetivoId).stream()
                .filter(a -> rol.vee(Estado.de(a.getEstado())))
                .map(a -> ActividadDTO.fromModel(a, null))
                .toList();
    }

    @Transactional
    public ActividadDTO crear(ActividadRequest request) {
        TipoPregunta tipo = TipoPregunta.de(request.getTipo());
        ActividadDesafio actividad = new ActividadDesafio(null, request.getObjetivoId(),
                request.getTitulo().trim(), tipo.name(),
                request.getDificultad() != null ? request.getDificultad() : 1,
                request.getOrden() != null ? request.getOrden() : 1,
                Estado.BORRADOR.name());
        return ActividadDTO.fromModel(actividadRepository.save(actividad), null);
    }

    @Transactional
    public ActividadDTO actualizar(Long id, ActividadRequest request) {
        ActividadDesafio actividad = porId(id);
        exigirBorrador(actividad);
        TipoPregunta tipo = TipoPregunta.de(request.getTipo());
        boolean tienePreguntas = !actividadPreguntaRepository.findByActividadIdOrderByOrdenAsc(id).isEmpty();
        if (tienePreguntas && !actividad.getTipo().equals(tipo.name())) {
            throw new ConflictException("No se puede cambiar el tipo de una actividad que ya tiene preguntas");
        }
        actividad.setTitulo(request.getTitulo().trim());
        actividad.setTipo(tipo.name());
        if (request.getDificultad() != null) {
            actividad.setDificultad(request.getDificultad());
        }
        if (request.getOrden() != null) {
            actividad.setOrden(request.getOrden());
        }
        return ActividadDTO.fromModel(actividadRepository.save(actividad), null);
    }

    /** Reemplaza la lista ordenada de preguntas. Cada una debe ser del mismo OA y del mismo tipo que la actividad. */
    @Transactional
    public List<PreguntaEnActividadDTO> reemplazarPreguntas(Long actividadId, List<PreguntaEnActividadDTO> items) {
        ActividadDesafio actividad = porId(actividadId);
        exigirBorrador(actividad);

        Set<Long> vistas = new HashSet<>();
        List<ActividadPregunta> nuevas = new ArrayList<>();
        int posicion = 1;
        for (PreguntaEnActividadDTO item : items) {
            Long preguntaId = item.getPreguntaId();
            if (preguntaId == null) {
                throw new BadRequestException("Cada elemento necesita preguntaId");
            }
            if (!vistas.add(preguntaId)) {
                throw new BadRequestException("La pregunta " + preguntaId + " está repetida");
            }
            Pregunta pregunta = preguntaRepository.findById(preguntaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada: " + preguntaId));
            if (!pregunta.getObjetivoAprendizajeId().equals(actividad.getObjetivoAprendizajeId())) {
                throw new BadRequestException("La pregunta " + preguntaId + " pertenece a otro objetivo");
            }
            if (!pregunta.getTipo().equals(actividad.getTipo())) {
                throw new BadRequestException("La pregunta " + preguntaId + " es de tipo " + pregunta.getTipo()
                        + " y la actividad es de tipo " + actividad.getTipo());
            }
            nuevas.add(new ActividadPregunta(null, actividadId, preguntaId,
                    item.getOrden() != null ? item.getOrden() : posicion));
            posicion++;
        }

        actividadPreguntaRepository.deleteByActividadId(actividadId);
        // Se vacía antes de insertar: Hibernate haría los inserts primero y chocaría con la restricción única.
        actividadPreguntaRepository.flush();
        actividadPreguntaRepository.saveAll(nuevas);
        return nuevas.stream().map(n -> new PreguntaEnActividadDTO(n.getPreguntaId(), n.getOrden())).toList();
    }

    private ActividadDesafio porId(Long id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada"));
    }

    private void exigirBorrador(ActividadDesafio actividad) {
        if (Estado.de(actividad.getEstado()) != Estado.BORRADOR) {
            throw new ConflictException("Solo se puede editar una actividad en estado BORRADOR");
        }
    }
}
