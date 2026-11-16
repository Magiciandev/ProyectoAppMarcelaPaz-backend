package com.mpaz.admin.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mpaz.admin.cliente.ContenidosClient;
import com.mpaz.admin.cliente.EvaluacionesClient;
import com.mpaz.admin.dto.AprobacionRequest;
import com.mpaz.admin.dto.AprobacionResponse;
import com.mpaz.admin.dto.HistorialDTO;
import com.mpaz.admin.dto.PaqueteContenidos;
import com.mpaz.admin.dto.PaqueteEvaluaciones;
import com.mpaz.admin.exception.BadGatewayException;
import com.mpaz.admin.exception.BadRequestException;
import com.mpaz.admin.exception.ConflictException;
import com.mpaz.admin.model.Accion;
import com.mpaz.admin.model.Estado;
import com.mpaz.admin.model.HistorialAprobacion;
import com.mpaz.admin.repository.HistorialRepository;

/**
 * Único lugar donde se valida el ciclo de estados de un objetivo de aprendizaje.
 * Cambia el estado en ms-contenidos y en ms-actividades-evaluaciones (reenviando el token de quien aprueba)
 * y deja el registro en el historial solo si ambos cambios se hicieron.
 */
@Service
public class AprobacionService {

    public static final String ENTIDAD_OBJETIVO = "OBJETIVO";

    private final ContenidosClient contenidos;
    private final EvaluacionesClient evaluaciones;
    private final HistorialRepository historialRepository;

    public AprobacionService(ContenidosClient contenidos, EvaluacionesClient evaluaciones,
                             HistorialRepository historialRepository) {
        this.contenidos = contenidos;
        this.evaluaciones = evaluaciones;
        this.historialRepository = historialRepository;
    }

    public AprobacionResponse aplicar(AprobacionRequest request, Long usuarioId, String authorization) {
        if (!ENTIDAD_OBJETIVO.equalsIgnoreCase(request.getEntidad())) {
            throw new BadRequestException("Solo se aprueban objetivos de aprendizaje (entidad OBJETIVO)");
        }
        Accion accion = Accion.de(request.getAccion());
        Long objetivoId = request.getEntidadId();

        PaqueteContenidos paquete = contenidos.obtenerPaquete(objetivoId, authorization);
        Estado actual = Estado.de(paquete.objetivo().estado());

        if (!accion.permiteDesde(actual)) {
            throw new ConflictException("No se puede aplicar " + accion + " a un objetivo en estado " + actual
                    + " (se requiere " + accion.desde() + ")");
        }
        if (accion == Accion.ENVIAR_REVISION) {
            exigirPrecondiciones(paquete, objetivoId, authorization);
        }

        Estado nuevo = accion.hasta();
        contenidos.fijarEstado(objetivoId, nuevo.name(), authorization);
        try {
            evaluaciones.fijarEstado(objetivoId, nuevo.name(), authorization);
        } catch (RuntimeException e) {
            // Mejor esfuerzo: dejar ms-contenidos como estaba para no quedar a medias.
            try {
                contenidos.fijarEstado(objetivoId, actual.name(), authorization);
            } catch (RuntimeException ignorada) {
                // si tampoco responde, el historial no se escribe y el estado quedará por revisar
            }
            throw new BadGatewayException("No se pudo completar el cambio de estado; se intentó revertir");
        }

        Instant ahora = Instant.now();
        HistorialAprobacion registro = new HistorialAprobacion(null, ENTIDAD_OBJETIVO, objetivoId, usuarioId,
                accion.name(), actual.name(), nuevo.name(), ahora, request.getObservacion());
        historialRepository.save(registro);

        return new AprobacionResponse(ENTIDAD_OBJETIVO, objetivoId, actual.name(), nuevo.name(), ahora);
    }

    public List<HistorialDTO> historial(String entidad, Long entidadId) {
        return historialRepository.findByEntidadAndEntidadIdOrderByFechaDesc(entidad.toUpperCase(), entidadId)
                .stream()
                .map(HistorialDTO::fromModel)
                .toList();
    }

    /** Para enviar a revisión: al menos un contenido y una prueba con al menos una pregunta. */
    private void exigirPrecondiciones(PaqueteContenidos paquete, Long objetivoId, String authorization) {
        if (paquete.cantidadContenidos() < 1) {
            throw new ConflictException("Para enviar a revisión se necesita al menos un contenido didáctico");
        }
        PaqueteEvaluaciones evaluacion = evaluaciones.obtenerPaquete(objetivoId, authorization);
        if (!evaluacion.tienePruebaConPreguntas()) {
            throw new ConflictException("Para enviar a revisión se necesita una prueba final con al menos una pregunta");
        }
    }
}
