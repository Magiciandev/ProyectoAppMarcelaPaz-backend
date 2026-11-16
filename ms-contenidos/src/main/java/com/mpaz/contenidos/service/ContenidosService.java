package com.mpaz.contenidos.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.contenidos.dto.AsignaturaDTO;
import com.mpaz.contenidos.dto.ContenidoDTO;
import com.mpaz.contenidos.dto.ContenidoRequest;
import com.mpaz.contenidos.dto.ObjetivoDTO;
import com.mpaz.contenidos.dto.ObjetivoRequest;
import com.mpaz.contenidos.dto.PaqueteDTO;
import com.mpaz.contenidos.dto.UnidadDTO;
import com.mpaz.contenidos.dto.UnidadRequest;
import com.mpaz.contenidos.exception.BadRequestException;
import com.mpaz.contenidos.exception.ConflictException;
import com.mpaz.contenidos.exception.ResourceNotFoundException;
import com.mpaz.contenidos.model.ContenidoDidactico;
import com.mpaz.contenidos.model.Estado;
import com.mpaz.contenidos.model.EstadoUnidadCalculator;
import com.mpaz.contenidos.model.ObjetivoAprendizaje;
import com.mpaz.contenidos.model.Rol;
import com.mpaz.contenidos.model.Unidad;
import com.mpaz.contenidos.repository.AsignaturaRepository;
import com.mpaz.contenidos.repository.ContenidoRepository;
import com.mpaz.contenidos.repository.ObjetivoRepository;
import com.mpaz.contenidos.repository.UnidadRepository;

@Service
public class ContenidosService {

    private final AsignaturaRepository asignaturaRepository;
    private final UnidadRepository unidadRepository;
    private final ObjetivoRepository objetivoRepository;
    private final ContenidoRepository contenidoRepository;

    public ContenidosService(AsignaturaRepository asignaturaRepository,
                             UnidadRepository unidadRepository,
                             ObjetivoRepository objetivoRepository,
                             ContenidoRepository contenidoRepository) {
        this.asignaturaRepository = asignaturaRepository;
        this.unidadRepository = unidadRepository;
        this.objetivoRepository = objetivoRepository;
        this.contenidoRepository = contenidoRepository;
    }

    // ---------------------------------------------------------------- lectura

    @Transactional(readOnly = true)
    public List<AsignaturaDTO> listarAsignaturas() {
        return asignaturaRepository.findAll().stream().map(AsignaturaDTO::fromModel).toList();
    }

    @Transactional(readOnly = true)
    public List<UnidadDTO> listarUnidades(Rol rol, Long asignaturaId) {
        List<Unidad> unidades = (asignaturaId == null)
                ? unidadRepository.findAllByOrderByOrdenAsc()
                : unidadRepository.findByAsignaturaIdOrderByOrdenAsc(asignaturaId);
        return unidades.stream()
                .filter(u -> rol.vee(Estado.de(u.getEstado())))
                .map(UnidadDTO::fromModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ObjetivoDTO> listarObjetivos(Rol rol, Long unidadId, Collection<Long> ids) {
        List<ObjetivoAprendizaje> base;
        if (ids != null && !ids.isEmpty()) {
            base = objetivoRepository.findByIdIn(ids);
        } else if (unidadId != null) {
            base = objetivoRepository.findByUnidadIdOrderByOrdenAsc(unidadId);
        } else {
            throw new BadRequestException("Indica unidadId o ids");
        }
        return base.stream()
                .filter(o -> rol.vee(Estado.de(o.getEstado())))
                .map(ObjetivoDTO::fromModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public ObjetivoDTO obtenerObjetivo(Rol rol, Long id) {
        return ObjetivoDTO.fromModel(objetivoVisible(rol, id));
    }

    @Transactional(readOnly = true)
    public PaqueteDTO paquete(Rol rol, Long id) {
        ObjetivoAprendizaje objetivo = objetivoVisible(rol, id);
        List<ContenidoDTO> contenidos = contenidoRepository.findByObjetivoIdOrderByOrdenAsc(id).stream()
                .map(ContenidoDTO::fromModel)
                .toList();
        return new PaqueteDTO(ObjetivoDTO.fromModel(objetivo), contenidos);
    }

    // ---------------------------------------------------------------- unidades

    @Transactional
    public UnidadDTO crearUnidad(UnidadRequest request, Long usuarioId) {
        if (request.getAsignaturaId() == null) {
            throw new BadRequestException("La asignatura es obligatoria");
        }
        if (!asignaturaRepository.existsById(request.getAsignaturaId())) {
            throw new ResourceNotFoundException("Asignatura no encontrada");
        }
        validarFechas(request.getFechaInicio(), request.getFechaFin());

        Unidad unidad = new Unidad();
        unidad.setAsignaturaId(request.getAsignaturaId());
        unidad.setTitulo(request.getTitulo().trim());
        unidad.setOrden(request.getOrden() != null ? request.getOrden() : 1);
        unidad.setEstado(Estado.BORRADOR.name());
        unidad.setFechaInicio(request.getFechaInicio());
        unidad.setFechaFin(request.getFechaFin());
        unidad.setCreadoPor(usuarioId);
        unidad.setCreadoEn(Instant.now());
        return UnidadDTO.fromModel(unidadRepository.save(unidad));
    }

    @Transactional
    public UnidadDTO actualizarUnidad(Long id, UnidadRequest request) {
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada"));
        validarFechas(request.getFechaInicio(), request.getFechaFin());
        unidad.setTitulo(request.getTitulo().trim());
        if (request.getOrden() != null) {
            unidad.setOrden(request.getOrden());
        }
        unidad.setFechaInicio(request.getFechaInicio());
        unidad.setFechaFin(request.getFechaFin());
        return UnidadDTO.fromModel(unidadRepository.save(unidad));
    }

    // ---------------------------------------------------------------- objetivos

    @Transactional
    public ObjetivoDTO crearObjetivo(Long unidadId, ObjetivoRequest request) {
        if (!unidadRepository.existsById(unidadId)) {
            throw new ResourceNotFoundException("Unidad no encontrada");
        }
        ObjetivoAprendizaje objetivo = new ObjetivoAprendizaje();
        objetivo.setUnidadId(unidadId);
        copiar(request, objetivo);
        objetivo.setEstado(Estado.BORRADOR.name());
        ObjetivoAprendizaje guardado = objetivoRepository.save(objetivo);
        recalcularUnidad(unidadId);
        return ObjetivoDTO.fromModel(guardado);
    }

    @Transactional
    public ObjetivoDTO actualizarObjetivo(Long id, ObjetivoRequest request) {
        ObjetivoAprendizaje objetivo = objetivoPorId(id);
        exigirBorrador(objetivo);
        copiar(request, objetivo);
        return ObjetivoDTO.fromModel(objetivoRepository.save(objetivo));
    }

    /**
     * Llamado solo por ms-admin (ruta interna): fija el estado del objetivo y recalcula el de su unidad.
     * La validación de la transición la hace ms-admin; aquí solo se comprueba que el estado exista.
     */
    @Transactional
    public ObjetivoDTO fijarEstadoObjetivo(Long id, String estadoNuevo) {
        Estado estado = Estado.de(estadoNuevo);
        ObjetivoAprendizaje objetivo = objetivoPorId(id);
        objetivo.setEstado(estado.name());
        objetivoRepository.save(objetivo);
        recalcularUnidad(objetivo.getUnidadId());
        return ObjetivoDTO.fromModel(objetivo);
    }

    // ---------------------------------------------------------------- contenidos

    @Transactional
    public ContenidoDTO crearContenido(Long objetivoId, ContenidoRequest request) {
        exigirBorrador(objetivoPorId(objetivoId));
        ContenidoDidactico contenido = new ContenidoDidactico();
        contenido.setObjetivoId(objetivoId);
        copiar(request, contenido);
        return ContenidoDTO.fromModel(contenidoRepository.save(contenido));
    }

    @Transactional
    public ContenidoDTO actualizarContenido(Long id, ContenidoRequest request) {
        ContenidoDidactico contenido = contenidoPorId(id);
        exigirBorrador(objetivoPorId(contenido.getObjetivoId()));
        copiar(request, contenido);
        return ContenidoDTO.fromModel(contenidoRepository.save(contenido));
    }

    @Transactional
    public void eliminarContenido(Long id) {
        ContenidoDidactico contenido = contenidoPorId(id);
        exigirBorrador(objetivoPorId(contenido.getObjetivoId()));
        contenidoRepository.delete(contenido);
    }

    // ---------------------------------------------------------------- auxiliares

    private ObjetivoAprendizaje objetivoPorId(Long id) {
        return objetivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Objetivo no encontrado"));
    }

    /** Si el rol no puede ver el estado del objetivo, responde como si no existiera (404). */
    private ObjetivoAprendizaje objetivoVisible(Rol rol, Long id) {
        return objetivoRepository.findById(id)
                .filter(o -> rol.vee(Estado.de(o.getEstado())))
                .orElseThrow(() -> new ResourceNotFoundException("Objetivo no encontrado"));
    }

    private ContenidoDidactico contenidoPorId(Long id) {
        return contenidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado"));
    }

    private void exigirBorrador(ObjetivoAprendizaje objetivo) {
        if (Estado.de(objetivo.getEstado()) != Estado.BORRADOR) {
            throw new ConflictException("Solo se puede editar un objetivo en estado BORRADOR");
        }
    }

    private void recalcularUnidad(Long unidadId) {
        Unidad unidad = unidadRepository.findById(unidadId)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad no encontrada"));
        List<Estado> estados = objetivoRepository.findByUnidadIdOrderByOrdenAsc(unidadId).stream()
                .map(o -> Estado.de(o.getEstado()))
                .toList();
        unidad.setEstado(EstadoUnidadCalculator.calcular(estados).name());
        unidadRepository.save(unidad);
    }

    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            throw new BadRequestException("La fecha de fin no puede ser anterior a la de inicio");
        }
    }

    private void copiar(ObjetivoRequest origen, ObjetivoAprendizaje destino) {
        destino.setCodigo(origen.getCodigo().trim());
        destino.setDescripcion(origen.getDescripcion().trim());
        destino.setEjeCurricular(origen.getEjeCurricular());
        destino.setTextoReferencia(origen.getTextoReferencia());
        destino.setOrden(origen.getOrden() != null ? origen.getOrden() : 1);
    }

    private void copiar(ContenidoRequest origen, ContenidoDidactico destino) {
        destino.setTitulo(origen.getTitulo().trim());
        destino.setExplicacion(origen.getExplicacion().trim());
        destino.setInstrucciones(origen.getInstrucciones());
        destino.setEjemplosGuiados(origen.getEjemplosGuiados());
        destino.setImagenUrl(origen.getImagenUrl());
        destino.setArchivoAudio(origen.getArchivoAudio());
        destino.setOrden(origen.getOrden() != null ? origen.getOrden() : 1);
        destino.setResponsableOrigen(origen.getResponsableOrigen().trim());
        destino.setLicencia(origen.getLicencia().trim());
    }
}
