package com.mpaz.progreso.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.progreso.cliente.ContenidosClient;
import com.mpaz.progreso.config.Reloj;
import com.mpaz.progreso.dto.AsignacionDTO;
import com.mpaz.progreso.dto.AsignacionRequest;
import com.mpaz.progreso.exception.BadRequestException;
import com.mpaz.progreso.exception.ConflictException;
import com.mpaz.progreso.exception.ResourceNotFoundException;
import com.mpaz.progreso.model.Asignacion;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.Rol;
import com.mpaz.progreso.repository.AsignacionRepository;
import com.mpaz.progreso.repository.EstudianteRepository;

@Service
public class AsignacionService {

    private static final int LIMITE_INTENTOS_POR_DEFECTO = 3;

    private final AsignacionRepository asignacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final AccesoService accesoService;
    private final ContenidosClient contenidosClient;
    private final Reloj reloj;

    public AsignacionService(AsignacionRepository asignacionRepository, EstudianteRepository estudianteRepository,
                             AccesoService accesoService, ContenidosClient contenidosClient, Reloj reloj) {
        this.asignacionRepository = asignacionRepository;
        this.estudianteRepository = estudianteRepository;
        this.accesoService = accesoService;
        this.contenidosClient = contenidosClient;
        this.reloj = reloj;
    }

    /**
     * Asignaciones disponibles que le corresponden a un estudiante (de su curso o individuales).
     * Si un objetivo está asignado a ambos niveles, manda la individual.
     */
    @Transactional(readOnly = true)
    public List<Asignacion> aplicables(Estudiante estudiante) {
        Map<Long, Asignacion> porObjetivo = new LinkedHashMap<>();
        for (Asignacion a : asignacionRepository.findByCursoIdAndDisponibleTrue(estudiante.getCursoId())) {
            porObjetivo.put(a.getObjetivoAprendizajeId(), a);
        }
        for (Asignacion a : asignacionRepository.findByEstudianteIdAndDisponibleTrue(estudiante.getUsuarioId())) {
            porObjetivo.put(a.getObjetivoAprendizajeId(), a);
        }
        return new ArrayList<>(porObjetivo.values());
    }

    @Transactional(readOnly = true)
    public Optional<Asignacion> buscar(Estudiante estudiante, Long objetivoId) {
        return aplicables(estudiante).stream()
                .filter(a -> a.getObjetivoAprendizajeId().equals(objetivoId))
                .findFirst();
    }

    @Transactional(readOnly = true)
    public List<AsignacionDTO> listarPorCurso(Rol rol, Long usuarioId, Long cursoId) {
        accesoService.cursoPermitido(cursoId, rol, usuarioId);
        List<Asignacion> resultado = new ArrayList<>(asignacionRepository.findByCursoId(cursoId));
        List<Long> estudiantes = estudianteRepository.findByCursoIdOrderByNombreAsc(cursoId).stream()
                .map(Estudiante::getUsuarioId).toList();
        if (!estudiantes.isEmpty()) {
            resultado.addAll(asignacionRepository.findByEstudianteIdIn(estudiantes));
        }
        return resultado.stream().map(AsignacionDTO::fromModel).toList();
    }

    @Transactional
    public AsignacionDTO crear(Long docenteId, AsignacionRequest request, String authorization) {
        validar(request);
        boolean porCurso = request.getCursoId() != null;
        if (porCurso) {
            accesoService.cursoPermitido(request.getCursoId(), Rol.DOCENTE, docenteId);
        } else {
            accesoService.estudiantePermitido(request.getEstudianteId(), Rol.DOCENTE, docenteId);
        }
        if (!contenidosClient.objetivoVisible(request.getObjetivoId(), authorization)) {
            throw new ResourceNotFoundException("Objetivo no encontrado o todavía no autorizado por UTP");
        }
        Optional<Asignacion> existente = porCurso
                ? asignacionRepository.findByObjetivoAprendizajeIdAndCursoId(request.getObjetivoId(), request.getCursoId())
                : asignacionRepository.findByObjetivoAprendizajeIdAndEstudianteId(request.getObjetivoId(), request.getEstudianteId());
        if (existente.isPresent()) {
            throw new ConflictException("Ese objetivo ya está asignado; edita la asignación existente");
        }

        Asignacion a = new Asignacion(null, request.getObjetivoId(), request.getCursoId(), request.getEstudianteId(),
                docenteId, request.getDisponible() == null || request.getDisponible(),
                request.getFechaDesde(), request.getFechaHasta(),
                request.getLimiteIntentos() != null ? request.getLimiteIntentos() : LIMITE_INTENTOS_POR_DEFECTO,
                request.getFechaHabilitacion(), request.getFechaRendicion(), Instant.now());
        return AsignacionDTO.fromModel(asignacionRepository.save(a));
    }

    @Transactional
    public AsignacionDTO actualizar(Long id, Long docenteId, AsignacionRequest request) {
        Asignacion a = asignacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada"));
        if (a.getCursoId() != null) {
            accesoService.cursoPermitido(a.getCursoId(), Rol.DOCENTE, docenteId);
        } else {
            accesoService.estudiantePermitido(a.getEstudianteId(), Rol.DOCENTE, docenteId);
        }
        validarFechas(request);
        a.setFechaDesde(request.getFechaDesde());
        a.setFechaHasta(request.getFechaHasta());
        a.setFechaHabilitacion(request.getFechaHabilitacion());
        a.setFechaRendicion(request.getFechaRendicion());
        if (request.getLimiteIntentos() != null) {
            a.setLimiteIntentos(request.getLimiteIntentos());
        }
        if (request.getDisponible() != null) {
            a.setDisponible(request.getDisponible());
        }
        return AsignacionDTO.fromModel(asignacionRepository.save(a));
    }

    private void validar(AsignacionRequest r) {
        if ((r.getCursoId() == null) == (r.getEstudianteId() == null)) {
            throw new BadRequestException("Indica el curso o el estudiante, pero no ambos");
        }
        validarFechas(r);
    }

    private void validarFechas(AsignacionRequest r) {
        if (r.getFechaDesde().isAfter(r.getFechaHasta())) {
            throw new BadRequestException("La fecha de inicio no puede ser posterior a la de fin");
        }
        if (r.getFechaHabilitacion() != null && r.getFechaRendicion() != null
                && r.getFechaHabilitacion().isAfter(r.getFechaRendicion())) {
            throw new BadRequestException("La habilitación de la prueba no puede ser posterior a su rendición");
        }
    }
}
