package com.mpaz.progreso.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.progreso.config.Reloj;
import com.mpaz.progreso.dto.IntentoSync;
import com.mpaz.progreso.dto.MejorObjetivoDTO;
import com.mpaz.progreso.dto.ProgresoSync;
import com.mpaz.progreso.dto.RespuestaSync;
import com.mpaz.progreso.dto.ResultadoIntento;
import com.mpaz.progreso.dto.ResultadoProgreso;
import com.mpaz.progreso.dto.SyncRequest;
import com.mpaz.progreso.dto.SyncResponse;
import com.mpaz.progreso.exception.ForbiddenException;
import com.mpaz.progreso.model.Asignacion;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.IntentoPrueba;
import com.mpaz.progreso.model.Progreso;
import com.mpaz.progreso.model.ReglasProgreso;
import com.mpaz.progreso.model.ReinicioProgreso;
import com.mpaz.progreso.model.RespuestaEstudiante;
import com.mpaz.progreso.repository.EstudianteRepository;
import com.mpaz.progreso.repository.IntentoRepository;
import com.mpaz.progreso.repository.ProgresoRepository;
import com.mpaz.progreso.repository.ReinicioRepository;
import com.mpaz.progreso.repository.RespuestaRepository;

/**
 * Recibe lo que el estudiante hizo sin conexión (contrato, sección 8). Cada elemento se procesa por separado y
 * recibe su propio resultado, de modo que reenviar un lote nunca duplica nada (idempotencia por uuid).
 */
@Service
public class SincronizacionService {

    public static final String ACEPTADO = "ACEPTADO";
    public static final String DUPLICADO = "DUPLICADO";
    public static final String RECHAZADO = "RECHAZADO";

    public static final String SIN_ASIGNACION = "SIN_ASIGNACION";
    public static final String FUERA_DE_FECHA = "FUERA_DE_FECHA";
    public static final String ANTERIOR_AL_REINICIO = "ANTERIOR_AL_REINICIO";
    public static final String LIMITE_INTENTOS = "LIMITE_INTENTOS";
    public static final String DATOS_INVALIDOS = "DATOS_INVALIDOS";

    private final EstudianteRepository estudianteRepository;
    private final IntentoRepository intentoRepository;
    private final RespuestaRepository respuestaRepository;
    private final ProgresoRepository progresoRepository;
    private final ReinicioRepository reinicioRepository;
    private final AsignacionService asignacionService;
    private final Reloj reloj;

    public SincronizacionService(EstudianteRepository estudianteRepository, IntentoRepository intentoRepository,
                                 RespuestaRepository respuestaRepository, ProgresoRepository progresoRepository,
                                 ReinicioRepository reinicioRepository, AsignacionService asignacionService,
                                 Reloj reloj) {
        this.estudianteRepository = estudianteRepository;
        this.intentoRepository = intentoRepository;
        this.respuestaRepository = respuestaRepository;
        this.progresoRepository = progresoRepository;
        this.reinicioRepository = reinicioRepository;
        this.asignacionService = asignacionService;
        this.reloj = reloj;
    }

    @Transactional
    public SyncResponse sincronizar(Long usuarioId, SyncRequest request) {
        Estudiante estudiante = estudianteRepository.findById(usuarioId)
                .orElseThrow(() -> new ForbiddenException("Tu usuario no está registrado como estudiante"));
        Instant ahora = reloj.ahora();
        List<ReinicioProgreso> reinicios = reinicioRepository.findByEstudianteIdOrderByFechaDesc(usuarioId);

        // Los intentos se procesan en el orden en que se hicieron (fecha del dispositivo): así valen los primeros N.
        List<IntentoSync> intentos = new ArrayList<>(request.getIntentos() == null ? List.of() : request.getIntentos());
        intentos.sort(Comparator.comparing(IntentoSync::getFechaHora, Comparator.nullsLast(Comparator.naturalOrder())));

        Set<Long> objetivosTocados = new LinkedHashSet<>();
        List<ResultadoIntento> resultadosIntentos = new ArrayList<>();
        for (IntentoSync intento : intentos) {
            resultadosIntentos.add(procesarIntento(estudiante, intento, reinicios, ahora));
            if (intento.getObjetivoId() != null) {
                objetivosTocados.add(intento.getObjetivoId());
            }
        }

        List<ResultadoProgreso> resultadosProgreso = new ArrayList<>();
        for (ProgresoSync progreso : request.getProgreso() == null ? List.<ProgresoSync>of() : request.getProgreso()) {
            resultadosProgreso.add(procesarProgreso(estudiante, progreso, reinicios));
            if (progreso.getObjetivoId() != null) {
                objetivosTocados.add(progreso.getObjetivoId());
            }
        }

        estudiante.setUltimaConexion(ahora);
        estudiante.setUltimaSincronizacion(ahora);
        estudianteRepository.save(estudiante);

        return new SyncResponse(resultadosIntentos, resultadosProgreso, mejoresPorObjetivo(estudiante, objetivosTocados),
                reinicios.isEmpty() ? null : reinicios.get(0).getFecha());
    }

    // ------------------------------------------------------------------ intentos

    private ResultadoIntento procesarIntento(Estudiante estudiante, IntentoSync in, List<ReinicioProgreso> reinicios,
                                             Instant ahora) {
        if (in.getUuid() == null || in.getUuid().isBlank() || in.getUuid().length() > 36) {
            return new ResultadoIntento(in.getUuid(), RECHAZADO, DATOS_INVALIDOS);
        }
        // Reenvío: lo ya recibido no se vuelve a evaluar.
        Optional<IntentoPrueba> previo = intentoRepository.findByUuid(in.getUuid());
        if (previo.isPresent()) {
            return previo.get().isAceptado()
                    ? new ResultadoIntento(in.getUuid(), DUPLICADO, null)
                    : new ResultadoIntento(in.getUuid(), RECHAZADO, previo.get().getMotivoRechazo());
        }
        if (!intentoValido(in)) {
            return new ResultadoIntento(in.getUuid(), RECHAZADO, DATOS_INVALIDOS);
        }

        String motivo = motivoDeRechazo(estudiante, in, reinicios);
        int porcentaje = ReglasProgreso.porcentaje(in.getPuntajeObtenido(), in.getPuntajeMaximo());
        IntentoPrueba guardado = intentoRepository.save(new IntentoPrueba(null, in.getUuid(), estudiante.getUsuarioId(),
                in.getPruebaId(), in.getObjetivoId(), in.getNumeroIntento(), in.getPuntajeObtenido(),
                in.getPuntajeMaximo(), porcentaje, in.getFechaHora(), ahora, motivo == null, motivo, false));

        if (motivo != null) {
            return new ResultadoIntento(in.getUuid(), RECHAZADO, motivo);
        }
        guardarRespuestas(guardado.getId(), in, ahora);
        return new ResultadoIntento(in.getUuid(), ACEPTADO, null);
    }

    /** Devuelve el motivo por el que se rechaza el intento, o null si se acepta. */
    private String motivoDeRechazo(Estudiante estudiante, IntentoSync in, List<ReinicioProgreso> reinicios) {
        Optional<Asignacion> asignacion = asignacionService.buscar(estudiante, in.getObjetivoId());
        if (asignacion.isEmpty()) {
            return SIN_ASIGNACION;
        }
        if (ReglasProgreso.fueraDeVentana(asignacion.get(), reloj.fechaDe(in.getFechaHora()))) {
            return FUERA_DE_FECHA;
        }
        if (ReglasProgreso.anteriorAlReinicio(reinicios, in.getObjetivoId(), in.getFechaHora())) {
            return ANTERIOR_AL_REINICIO;
        }
        long aceptados = intentoRepository.countByEstudianteIdAndObjetivoAprendizajeIdAndAceptadoTrueAndArchivadoFalse(
                estudiante.getUsuarioId(), in.getObjetivoId());
        if (aceptados >= asignacion.get().getLimiteIntentos()) {
            return LIMITE_INTENTOS;
        }
        return null;
    }

    private boolean intentoValido(IntentoSync in) {
        return in.getPruebaId() != null && in.getObjetivoId() != null && in.getFechaHora() != null
                && in.getNumeroIntento() != null && in.getNumeroIntento() >= 1
                && in.getPuntajeObtenido() != null && in.getPuntajeMaximo() != null
                && in.getPuntajeMaximo().signum() > 0
                && in.getPuntajeObtenido().signum() >= 0
                && in.getPuntajeObtenido().compareTo(in.getPuntajeMaximo()) <= 0;
    }

    private void guardarRespuestas(Long intentoId, IntentoSync in, Instant ahora) {
        if (in.getRespuestas() == null) {
            return;
        }
        for (RespuestaSync r : in.getRespuestas()) {
            boolean valida = r.getUuid() != null && !r.getUuid().isBlank() && r.getUuid().length() <= 36
                    && r.getPreguntaId() != null && r.getRespuesta() != null && r.getEsCorrecta() != null;
            if (!valida || respuestaRepository.existsByUuid(r.getUuid())) {
                continue;
            }
            respuestaRepository.save(new RespuestaEstudiante(null, r.getUuid(), intentoId, r.getPreguntaId(),
                    r.getRespuesta(), r.getEsCorrecta(),
                    r.getPuntajeObtenido() != null ? r.getPuntajeObtenido() : BigDecimal.ZERO,
                    r.getMarcaTiempo() != null ? r.getMarcaTiempo() : ahora));
        }
    }

    // ------------------------------------------------------------------ progreso

    private ResultadoProgreso procesarProgreso(Estudiante estudiante, ProgresoSync p, List<ReinicioProgreso> reinicios) {
        if (p.getObjetivoId() == null) {
            return new ResultadoProgreso(null, RECHAZADO, DATOS_INVALIDOS);
        }
        boolean valido = p.getMarcaTiempo() != null && p.getPorcentajeAvance() != null
                && p.getPorcentajeAvance() >= 0 && p.getPorcentajeAvance() <= 100
                && nuloOCero(p.getContenidosCompletados()) >= 0 && nuloOCero(p.getActividadesCompletadas()) >= 0
                && nuloOCero(p.getDesafiosCompletados()) >= 0;
        if (!valido) {
            return new ResultadoProgreso(p.getObjetivoId(), RECHAZADO, DATOS_INVALIDOS);
        }
        if (asignacionService.buscar(estudiante, p.getObjetivoId()).isEmpty()) {
            return new ResultadoProgreso(p.getObjetivoId(), RECHAZADO, SIN_ASIGNACION);
        }
        if (ReglasProgreso.anteriorAlReinicio(reinicios, p.getObjetivoId(), p.getMarcaTiempo())) {
            return new ResultadoProgreso(p.getObjetivoId(), RECHAZADO, ANTERIOR_AL_REINICIO);
        }
        Optional<Progreso> existente = progresoRepository
                .findByEstudianteIdAndObjetivoAprendizajeId(estudiante.getUsuarioId(), p.getObjetivoId());
        if (existente.isPresent() && !existente.get().getMarcaTiempo().isBefore(p.getMarcaTiempo())) {
            return new ResultadoProgreso(p.getObjetivoId(), DUPLICADO, null); // ya hay algo más reciente
        }
        Progreso fila = existente.orElseGet(() -> {
            Progreso nuevo = new Progreso();
            nuevo.setEstudianteId(estudiante.getUsuarioId());
            nuevo.setObjetivoAprendizajeId(p.getObjetivoId());
            return nuevo;
        });
        fila.setContenidosCompletados(nuloOCero(p.getContenidosCompletados()));
        fila.setActividadesCompletadas(nuloOCero(p.getActividadesCompletadas()));
        fila.setDesafiosCompletados(nuloOCero(p.getDesafiosCompletados()));
        fila.setPorcentajeAvance(p.getPorcentajeAvance());
        fila.setMarcaTiempo(p.getMarcaTiempo());
        progresoRepository.save(fila);
        return new ResultadoProgreso(p.getObjetivoId(), ACEPTADO, null);
    }

    private int nuloOCero(Integer valor) {
        return valor == null ? 0 : valor;
    }

    // ------------------------------------------------------------------ mejor resultado

    private List<MejorObjetivoDTO> mejoresPorObjetivo(Estudiante estudiante, Set<Long> objetivos) {
        List<MejorObjetivoDTO> resultado = new ArrayList<>();
        for (Long objetivoId : objetivos) {
            List<IntentoPrueba> aceptados = intentoRepository
                    .findByEstudianteIdAndObjetivoAprendizajeIdAndAceptadoTrueAndArchivadoFalse(
                            estudiante.getUsuarioId(), objetivoId);
            Integer mejor = aceptados.stream().map(IntentoPrueba::getPorcentaje).max(Integer::compare).orElse(null);
            Integer limite = asignacionService.buscar(estudiante, objetivoId).map(Asignacion::getLimiteIntentos).orElse(null);
            resultado.add(new MejorObjetivoDTO(objetivoId, mejor, aceptados.size(), limite));
        }
        return resultado;
    }
}
