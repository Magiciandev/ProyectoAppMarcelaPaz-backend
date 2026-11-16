package com.mpaz.progreso.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.progreso.config.Reloj;
import com.mpaz.progreso.dto.InformeDTO;
import com.mpaz.progreso.dto.InformeObjetivoDTO;
import com.mpaz.progreso.dto.InformeRequest;
import com.mpaz.progreso.dto.InformeRespuestaDTO;
import com.mpaz.progreso.dto.ResumenDTO;
import com.mpaz.progreso.dto.ResumenObjetivoDTO;
import com.mpaz.progreso.dto.ValidacionRequest;
import com.mpaz.progreso.exception.BadRequestException;
import com.mpaz.progreso.exception.ConflictException;
import com.mpaz.progreso.exception.ResourceNotFoundException;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.InformeObjetivo;
import com.mpaz.progreso.model.InformePedagogico;
import com.mpaz.progreso.model.InformeRespuesta;
import com.mpaz.progreso.model.IntentoPrueba;
import com.mpaz.progreso.model.NivelLogro;
import com.mpaz.progreso.model.ReglasProgreso;
import com.mpaz.progreso.model.Rol;
import com.mpaz.progreso.repository.InformeObjetivoRepository;
import com.mpaz.progreso.repository.InformeRepository;
import com.mpaz.progreso.repository.InformeRespuestaRepository;
import com.mpaz.progreso.repository.RespuestaRepository;

/**
 * Informes pedagógicos: son del docente. El nivel sale de una regla fija sobre el mejor porcentaje (sin diagnóstico
 * automático); la observación la escribe el docente y el informe queda validado solo cuando él lo valida.
 */
@Service
public class InformeService {

    private final InformeRepository informeRepository;
    private final InformeObjetivoRepository informeObjetivoRepository;
    private final InformeRespuestaRepository informeRespuestaRepository;
    private final RespuestaRepository respuestaRepository;
    private final ResumenService resumenService;
    private final AccesoService accesoService;
    private final Reloj reloj;

    public InformeService(InformeRepository informeRepository, InformeObjetivoRepository informeObjetivoRepository,
                          InformeRespuestaRepository informeRespuestaRepository, RespuestaRepository respuestaRepository,
                          ResumenService resumenService, AccesoService accesoService, Reloj reloj) {
        this.informeRepository = informeRepository;
        this.informeObjetivoRepository = informeObjetivoRepository;
        this.informeRespuestaRepository = informeRespuestaRepository;
        this.respuestaRepository = respuestaRepository;
        this.resumenService = resumenService;
        this.accesoService = accesoService;
        this.reloj = reloj;
    }

    @Transactional
    public InformeDTO generar(Long docenteId, InformeRequest request) {
        Estudiante estudiante = accesoService.estudiantePermitido(request.getEstudianteId(), Rol.DOCENTE, docenteId);
        ResumenDTO resumen = resumenService.resumen(estudiante);

        List<ResumenObjetivoDTO> considerados = resumen.getObjetivos().stream()
                .filter(o -> o.getMejorPorcentaje() != null)
                .filter(o -> request.getObjetivoId() == null || o.getObjetivoId().equals(request.getObjetivoId()))
                .toList();
        if (considerados.isEmpty()) {
            throw new ConflictException(request.getObjetivoId() == null
                    ? "El estudiante todavía no rinde ninguna prueba"
                    : "El estudiante todavía no rinde la prueba de ese objetivo");
        }

        int promedio = ReglasProgreso.promedio(considerados.stream().map(ResumenObjetivoDTO::getMejorPorcentaje).toList());
        InformePedagogico informe = informeRepository.save(new InformePedagogico(null, estudiante.getUsuarioId(),
                estudiante.getCursoId(), request.getObjetivoId(), BigDecimal.valueOf(promedio),
                NivelLogro.de(promedio).name(), reloj.ahora(), false, null, null, null));

        Map<Long, List<IntentoPrueba>> aceptados = resumenService.aceptadosPorObjetivo(estudiante.getUsuarioId());
        for (ResumenObjetivoDTO o : considerados) {
            informeObjetivoRepository.save(new InformeObjetivo(null, informe.getId(), o.getObjetivoId(),
                    o.getMejorPorcentaje(), o.getNivel(), (int) o.getIntentosAceptados()));
            // Desglose de respuestas: las del mejor intento (si empatan, el más antiguo).
            aceptados.getOrDefault(o.getObjetivoId(), List.of()).stream()
                    .max(Comparator.comparing(IntentoPrueba::getPorcentaje)
                            .thenComparing(IntentoPrueba::getFechaHoraCliente, Comparator.reverseOrder()))
                    .ifPresent(mejor -> respuestaRepository.findByIntentoId(mejor.getId()).forEach(r ->
                            informeRespuestaRepository.save(new InformeRespuesta(null, informe.getId(),
                                    o.getObjetivoId(), r.getPreguntaId(), r.isEsCorrecta(), r.getPuntajeObtenido()))));
        }
        return conDetalle(informe);
    }

    @Transactional(readOnly = true)
    public List<InformeDTO> listar(Rol rol, Long usuarioId, Long cursoId, Long estudianteId) {
        List<InformePedagogico> informes;
        if (estudianteId != null) {
            Estudiante e = accesoService.estudiantePermitido(estudianteId, rol, usuarioId);
            informes = informeRepository.findByEstudianteIdOrderByFechaEmisionDesc(e.getUsuarioId());
        } else if (cursoId != null) {
            accesoService.cursoPermitido(cursoId, rol, usuarioId);
            informes = informeRepository.findByCursoIdOrderByFechaEmisionDesc(cursoId);
        } else {
            throw new BadRequestException("Indica cursoId o estudianteId");
        }
        return informes.stream().map(this::sinDetalle).toList();
    }

    @Transactional(readOnly = true)
    public InformeDTO obtener(Rol rol, Long usuarioId, Long id) {
        InformePedagogico informe = porId(id);
        accesoService.cursoPermitido(informe.getCursoId(), rol, usuarioId);
        return conDetalle(informe);
    }

    @Transactional
    public InformeDTO validar(Long id, Long docenteId, ValidacionRequest request) {
        InformePedagogico informe = porId(id);
        accesoService.cursoPermitido(informe.getCursoId(), Rol.DOCENTE, docenteId);
        if (request.getObservacion() != null) {
            informe.setObservacionDocente(request.getObservacion());
        }
        if (Boolean.TRUE.equals(request.getValidar())) {
            informe.setValidado(true);
            informe.setValidadoPor(docenteId);
            informe.setFechaValidacion(reloj.ahora());
        }
        informeRepository.save(informe);
        return conDetalle(informe);
    }

    // ------------------------------------------------------------------ auxiliares

    private InformePedagogico porId(Long id) {
        return informeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Informe no encontrado"));
    }

    private InformeDTO sinDetalle(InformePedagogico i) {
        return new InformeDTO(i.getId(), i.getEstudianteId(), i.getCursoId(), i.getObjetivoAprendizajeId(),
                i.getPuntajeGlobal(), i.getNivel(), i.getFechaEmision(), i.isValidado(), i.getValidadoPor(),
                i.getFechaValidacion(), i.getObservacionDocente(), null, null, null);
    }

    private InformeDTO conDetalle(InformePedagogico i) {
        List<InformeObjetivoDTO> porObjetivo = informeObjetivoRepository.findByInformeId(i.getId()).stream()
                .map(o -> new InformeObjetivoDTO(o.getObjetivoId(), o.getMejorPorcentaje(), o.getNivel(),
                        o.getIntentosAceptados()))
                .toList();
        List<Long> areasRefuerzo = porObjetivo.stream()
                .filter(o -> NivelLogro.REFUERZO.name().equals(o.getNivel()))
                .map(InformeObjetivoDTO::getObjetivoId)
                .toList();
        List<InformeRespuestaDTO> respuestas = informeRespuestaRepository.findByInformeId(i.getId()).stream()
                .map(r -> new InformeRespuestaDTO(r.getObjetivoId(), r.getPreguntaId(), r.isEsCorrecta(), r.getPuntaje()))
                .toList();
        return new InformeDTO(i.getId(), i.getEstudianteId(), i.getCursoId(), i.getObjetivoAprendizajeId(),
                i.getPuntajeGlobal(), i.getNivel(), i.getFechaEmision(), i.isValidado(), i.getValidadoPor(),
                i.getFechaValidacion(), i.getObservacionDocente(), porObjetivo, areasRefuerzo, respuestas);
    }
}
