package com.mpaz.evaluaciones.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.evaluaciones.dto.OpcionDTO;
import com.mpaz.evaluaciones.dto.PreguntaDTO;
import com.mpaz.evaluaciones.dto.PreguntaRequest;
import com.mpaz.evaluaciones.exception.ConflictException;
import com.mpaz.evaluaciones.exception.ResourceNotFoundException;
import com.mpaz.evaluaciones.model.ActividadPregunta;
import com.mpaz.evaluaciones.model.Estado;
import com.mpaz.evaluaciones.model.Opcion;
import com.mpaz.evaluaciones.model.Pregunta;
import com.mpaz.evaluaciones.model.PruebaPregunta;
import com.mpaz.evaluaciones.model.TipoPregunta;
import com.mpaz.evaluaciones.model.ValidadorPregunta;
import com.mpaz.evaluaciones.repository.ActividadPreguntaRepository;
import com.mpaz.evaluaciones.repository.ActividadRepository;
import com.mpaz.evaluaciones.repository.OpcionRepository;
import com.mpaz.evaluaciones.repository.PreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaPreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaRepository;

/** Banco de preguntas por objetivo de aprendizaje. */
@Service
public class PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final OpcionRepository opcionRepository;
    private final ActividadRepository actividadRepository;
    private final ActividadPreguntaRepository actividadPreguntaRepository;
    private final PruebaRepository pruebaRepository;
    private final PruebaPreguntaRepository pruebaPreguntaRepository;

    public PreguntaService(PreguntaRepository preguntaRepository, OpcionRepository opcionRepository,
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

    @Transactional(readOnly = true)
    public List<PreguntaDTO> listar(Long objetivoId) {
        return aDtos(preguntaRepository.findByObjetivoAprendizajeIdOrderByIdAsc(objetivoId));
    }

    @Transactional
    public PreguntaDTO crear(PreguntaRequest request) {
        TipoPregunta tipo = TipoPregunta.de(request.getTipo());
        ValidadorPregunta.validar(tipo, request.getOpciones());

        Pregunta pregunta = new Pregunta(null, request.getObjetivoId(), tipo.name(),
                request.getEnunciado().trim(), request.getExplicacion());
        Pregunta guardada = preguntaRepository.save(pregunta);
        guardarOpciones(guardada.getId(), tipo, request.getOpciones());
        return PreguntaDTO.fromModel(guardada, opcionRepository.findByPreguntaIdOrderByOrdenAsc(guardada.getId()));
    }

    @Transactional
    public PreguntaDTO actualizar(Long id, PreguntaRequest request) {
        Pregunta pregunta = preguntaPorId(id);
        TipoPregunta tipo = TipoPregunta.de(request.getTipo());
        ValidadorPregunta.validar(tipo, request.getOpciones());

        exigirQueNoEsteEnUsoFueraDeBorrador(id);
        boolean enUso = !actividadPreguntaRepository.findByPreguntaId(id).isEmpty()
                || !pruebaPreguntaRepository.findByPreguntaId(id).isEmpty();
        boolean cambiaIdentidad = !pregunta.getObjetivoAprendizajeId().equals(request.getObjetivoId())
                || !pregunta.getTipo().equals(tipo.name());
        if (enUso && cambiaIdentidad) {
            throw new ConflictException("No se puede cambiar el objetivo ni el tipo de una pregunta ya usada");
        }

        pregunta.setObjetivoAprendizajeId(request.getObjetivoId());
        pregunta.setTipo(tipo.name());
        pregunta.setEnunciado(request.getEnunciado().trim());
        pregunta.setExplicacion(request.getExplicacion());
        preguntaRepository.save(pregunta);

        opcionRepository.deleteByPreguntaId(id);
        guardarOpciones(id, tipo, request.getOpciones());
        return PreguntaDTO.fromModel(pregunta, opcionRepository.findByPreguntaIdOrderByOrdenAsc(id));
    }

    @Transactional
    public void eliminar(Long id) {
        Pregunta pregunta = preguntaPorId(id);
        boolean enUso = !actividadPreguntaRepository.findByPreguntaId(id).isEmpty()
                || !pruebaPreguntaRepository.findByPreguntaId(id).isEmpty();
        if (enUso) {
            throw new ConflictException("La pregunta está en una actividad o prueba; quítala de ahí primero");
        }
        opcionRepository.deleteByPreguntaId(id);
        preguntaRepository.delete(pregunta);
    }

    /** Arma los DTO de varias preguntas con sus opciones usando una sola consulta de opciones. */
    @Transactional(readOnly = true)
    public List<PreguntaDTO> aDtos(Collection<Pregunta> preguntas) {
        if (preguntas.isEmpty()) {
            return List.of();
        }
        List<Long> ids = preguntas.stream().map(Pregunta::getId).toList();
        Map<Long, List<Opcion>> porPregunta = opcionRepository.findByPreguntaIdInOrderByOrdenAsc(ids).stream()
                .collect(Collectors.groupingBy(Opcion::getPreguntaId));
        return preguntas.stream()
                .map(p -> PreguntaDTO.fromModel(p, porPregunta.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    // ------------------------------------------------------------------ auxiliares

    private Pregunta preguntaPorId(Long id) {
        return preguntaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada"));
    }

    private void guardarOpciones(Long preguntaId, TipoPregunta tipo, List<OpcionDTO> opciones) {
        int posicion = 1;
        for (OpcionDTO o : opciones) {
            boolean correcta = tipo.usaEsCorrecta() && Boolean.TRUE.equals(o.getEsCorrecta());
            Integer posicionCorrecta = tipo == TipoPregunta.SECUENCIA ? o.getPosicionCorrecta() : null;
            String parClave = tipo == TipoPregunta.ASOCIACION ? o.getParClave().trim() : null;
            Integer orden = o.getOrden() != null ? o.getOrden() : posicion;
            opcionRepository.save(new Opcion(null, preguntaId, o.getTexto().trim(), correcta,
                    posicionCorrecta, parClave, orden));
            posicion++;
        }
    }

    /** Una pregunta no se edita si está en una actividad o prueba que ya salió de BORRADOR. */
    private void exigirQueNoEsteEnUsoFueraDeBorrador(Long preguntaId) {
        for (ActividadPregunta vinculo : actividadPreguntaRepository.findByPreguntaId(preguntaId)) {
            actividadRepository.findById(vinculo.getActividadId()).ifPresent(a -> {
                if (Estado.de(a.getEstado()) != Estado.BORRADOR) {
                    throw new ConflictException("La pregunta está en una actividad que no está en BORRADOR");
                }
            });
        }
        for (PruebaPregunta vinculo : pruebaPreguntaRepository.findByPreguntaId(preguntaId)) {
            pruebaRepository.findById(vinculo.getPruebaId()).ifPresent(p -> {
                if (Estado.de(p.getEstado()) != Estado.BORRADOR) {
                    throw new ConflictException("La pregunta está en una prueba que no está en BORRADOR");
                }
            });
        }
    }
}
