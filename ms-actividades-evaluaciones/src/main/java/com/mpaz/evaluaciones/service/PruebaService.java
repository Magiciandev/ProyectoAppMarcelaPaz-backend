package com.mpaz.evaluaciones.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.evaluaciones.dto.PreguntaEnPruebaDTO;
import com.mpaz.evaluaciones.dto.PruebaDTO;
import com.mpaz.evaluaciones.dto.PruebaRequest;
import com.mpaz.evaluaciones.exception.BadRequestException;
import com.mpaz.evaluaciones.exception.ConflictException;
import com.mpaz.evaluaciones.exception.ResourceNotFoundException;
import com.mpaz.evaluaciones.model.Estado;
import com.mpaz.evaluaciones.model.Pregunta;
import com.mpaz.evaluaciones.model.PruebaFinal;
import com.mpaz.evaluaciones.model.PruebaPregunta;
import com.mpaz.evaluaciones.model.Rol;
import com.mpaz.evaluaciones.repository.PreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaPreguntaRepository;
import com.mpaz.evaluaciones.repository.PruebaRepository;

@Service
public class PruebaService {

    private static final int LIMITE_INTENTOS_POR_DEFECTO = 3;

    private final PruebaRepository pruebaRepository;
    private final PruebaPreguntaRepository pruebaPreguntaRepository;
    private final PreguntaRepository preguntaRepository;

    public PruebaService(PruebaRepository pruebaRepository, PruebaPreguntaRepository pruebaPreguntaRepository,
                         PreguntaRepository preguntaRepository) {
        this.pruebaRepository = pruebaRepository;
        this.pruebaPreguntaRepository = pruebaPreguntaRepository;
        this.preguntaRepository = preguntaRepository;
    }

    /** La prueba final del objetivo, si el rol puede verla en su estado actual (si no, 404). */
    @Transactional(readOnly = true)
    public PruebaDTO obtenerPorObjetivo(Rol rol, Long objetivoId) {
        PruebaFinal prueba = pruebaRepository.findByObjetivoAprendizajeId(objetivoId)
                .filter(p -> rol.vee(Estado.de(p.getEstado())))
                .orElseThrow(() -> new ResourceNotFoundException("Prueba final no encontrada"));
        return PruebaDTO.fromModel(prueba, preguntasDe(prueba.getId()));
    }

    @Transactional
    public PruebaDTO crear(PruebaRequest request) {
        if (pruebaRepository.findByObjetivoAprendizajeId(request.getObjetivoId()).isPresent()) {
            throw new ConflictException("Ese objetivo ya tiene una prueba final");
        }
        PruebaFinal prueba = new PruebaFinal(null, request.getObjetivoId(), request.getTitulo().trim(),
                request.getCriteriosPuntuacion(),
                request.getLimiteIntentos() != null ? request.getLimiteIntentos() : LIMITE_INTENTOS_POR_DEFECTO,
                Estado.BORRADOR.name());
        return PruebaDTO.fromModel(pruebaRepository.save(prueba), List.of());
    }

    @Transactional
    public PruebaDTO actualizar(Long id, PruebaRequest request) {
        PruebaFinal prueba = porId(id);
        exigirBorrador(prueba);
        prueba.setTitulo(request.getTitulo().trim());
        prueba.setCriteriosPuntuacion(request.getCriteriosPuntuacion());
        if (request.getLimiteIntentos() != null) {
            prueba.setLimiteIntentos(request.getLimiteIntentos());
        }
        pruebaRepository.save(prueba);
        return PruebaDTO.fromModel(prueba, preguntasDe(id));
    }

    /** Reemplaza las preguntas de la prueba con su orden y puntaje. Todas deben ser del mismo objetivo. */
    @Transactional
    public List<PreguntaEnPruebaDTO> reemplazarPreguntas(Long pruebaId, List<PreguntaEnPruebaDTO> items) {
        PruebaFinal prueba = porId(pruebaId);
        exigirBorrador(prueba);

        Set<Long> vistas = new HashSet<>();
        List<PruebaPregunta> nuevas = new ArrayList<>();
        int posicion = 1;
        for (PreguntaEnPruebaDTO item : items) {
            Long preguntaId = item.getPreguntaId();
            if (preguntaId == null) {
                throw new BadRequestException("Cada elemento necesita preguntaId");
            }
            if (!vistas.add(preguntaId)) {
                throw new BadRequestException("La pregunta " + preguntaId + " está repetida");
            }
            Pregunta pregunta = preguntaRepository.findById(preguntaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pregunta no encontrada: " + preguntaId));
            if (!pregunta.getObjetivoAprendizajeId().equals(prueba.getObjetivoAprendizajeId())) {
                throw new BadRequestException("La pregunta " + preguntaId + " pertenece a otro objetivo");
            }
            BigDecimal puntaje = item.getPuntaje() != null ? item.getPuntaje() : BigDecimal.ONE;
            if (puntaje.signum() <= 0) {
                throw new BadRequestException("El puntaje debe ser mayor que 0");
            }
            nuevas.add(new PruebaPregunta(null, pruebaId, preguntaId,
                    item.getOrdenEnPrueba() != null ? item.getOrdenEnPrueba() : posicion, puntaje));
            posicion++;
        }

        pruebaPreguntaRepository.deleteByPruebaId(pruebaId);
        // Se vacía antes de insertar: Hibernate haría los inserts primero y chocaría con la restricción única.
        pruebaPreguntaRepository.flush();
        pruebaPreguntaRepository.saveAll(nuevas);
        return nuevas.stream()
                .map(n -> new PreguntaEnPruebaDTO(n.getPreguntaId(), n.getOrdenEnPrueba(), n.getPuntaje()))
                .toList();
    }

    private List<PreguntaEnPruebaDTO> preguntasDe(Long pruebaId) {
        return pruebaPreguntaRepository.findByPruebaIdOrderByOrdenEnPruebaAsc(pruebaId).stream()
                .map(pp -> new PreguntaEnPruebaDTO(pp.getPreguntaId(), pp.getOrdenEnPrueba(), pp.getPuntaje()))
                .toList();
    }

    private PruebaFinal porId(Long id) {
        return pruebaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prueba final no encontrada"));
    }

    private void exigirBorrador(PruebaFinal prueba) {
        if (Estado.de(prueba.getEstado()) != Estado.BORRADOR) {
            throw new ConflictException("Solo se puede editar una prueba en estado BORRADOR");
        }
    }
}
