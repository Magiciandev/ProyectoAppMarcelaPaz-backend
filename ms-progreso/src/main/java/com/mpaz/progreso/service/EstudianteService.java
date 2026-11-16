package com.mpaz.progreso.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mpaz.progreso.config.Reloj;
import com.mpaz.progreso.dto.AsignacionEstudianteDTO;
import com.mpaz.progreso.dto.AvanceDTO;
import com.mpaz.progreso.dto.AvanceObjetivoDTO;
import com.mpaz.progreso.dto.CursoDTO;
import com.mpaz.progreso.dto.EstudianteCursoDTO;
import com.mpaz.progreso.dto.EstudianteDTO;
import com.mpaz.progreso.exception.ForbiddenException;
import com.mpaz.progreso.exception.ResourceNotFoundException;
import com.mpaz.progreso.model.Asignacion;
import com.mpaz.progreso.model.Curso;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.Progreso;
import com.mpaz.progreso.model.ReglasProgreso;
import com.mpaz.progreso.model.ReinicioProgreso;
import com.mpaz.progreso.model.Rol;
import com.mpaz.progreso.repository.CursoRepository;
import com.mpaz.progreso.repository.EstudianteRepository;
import com.mpaz.progreso.repository.IntentoRepository;
import com.mpaz.progreso.repository.ProgresoRepository;
import com.mpaz.progreso.repository.ReinicioRepository;

/** Lo que ve cada estudiante de sí mismo, y los listados de cursos y estudiantes para docente y UTP. */
@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final CursoRepository cursoRepository;
    private final ProgresoRepository progresoRepository;
    private final IntentoRepository intentoRepository;
    private final ReinicioRepository reinicioRepository;
    private final AsignacionService asignacionService;
    private final AccesoService accesoService;
    private final Reloj reloj;

    public EstudianteService(EstudianteRepository estudianteRepository, CursoRepository cursoRepository,
                             ProgresoRepository progresoRepository, IntentoRepository intentoRepository,
                             ReinicioRepository reinicioRepository, AsignacionService asignacionService,
                             AccesoService accesoService, Reloj reloj) {
        this.estudianteRepository = estudianteRepository;
        this.cursoRepository = cursoRepository;
        this.progresoRepository = progresoRepository;
        this.intentoRepository = intentoRepository;
        this.reinicioRepository = reinicioRepository;
        this.asignacionService = asignacionService;
        this.accesoService = accesoService;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------ estudiante

    @Transactional(readOnly = true)
    public EstudianteDTO me(Long usuarioId) {
        Estudiante e = estudiante(usuarioId);
        String curso = cursoRepository.findById(e.getCursoId()).map(Curso::getNombre).orElse(null);
        return new EstudianteDTO(e.getUsuarioId(), e.getNombre(), e.getCursoId(), curso);
    }

    @Transactional
    public void registrarConexion(Long usuarioId) {
        Estudiante e = estudiante(usuarioId);
        e.setUltimaConexion(reloj.ahora());
        estudianteRepository.save(e);
    }

    /** Asignaciones cuya ventana (desde-hasta) incluye hoy. */
    @Transactional(readOnly = true)
    public List<AsignacionEstudianteDTO> asignacionesVigentes(Long usuarioId) {
        Estudiante e = estudiante(usuarioId);
        var hoy = reloj.hoy();
        return asignacionService.aplicables(e).stream()
                .filter(a -> !hoy.isBefore(a.getFechaDesde()) && !hoy.isAfter(a.getFechaHasta()))
                .map(a -> new AsignacionEstudianteDTO(a.getId(), a.getObjetivoAprendizajeId(), a.getFechaDesde(),
                        a.getFechaHasta(), a.getLimiteIntentos(), a.getFechaHabilitacion(), a.getFechaRendicion(),
                        intentoRepository.countByEstudianteIdAndObjetivoAprendizajeIdAndAceptadoTrueAndArchivadoFalse(
                                usuarioId, a.getObjetivoAprendizajeId())))
                .toList();
    }

    /** Avance e intentos propios. No incluye niveles ni informes (son del docente). */
    @Transactional(readOnly = true)
    public AvanceDTO avance(Long usuarioId) {
        Estudiante e = estudiante(usuarioId);
        Map<Long, Progreso> progresos = progresoRepository.findByEstudianteId(usuarioId).stream()
                .collect(Collectors.toMap(Progreso::getObjetivoAprendizajeId, p -> p, (a, b) -> a));
        List<AvanceObjetivoDTO> objetivos = asignacionService.aplicables(e).stream().map(a -> {
            long usados = intentoRepository.countByEstudianteIdAndObjetivoAprendizajeIdAndAceptadoTrueAndArchivadoFalse(
                    usuarioId, a.getObjetivoAprendizajeId());
            Progreso p = progresos.get(a.getObjetivoAprendizajeId());
            return new AvanceObjetivoDTO(a.getObjetivoAprendizajeId(), p != null ? p.getPorcentajeAvance() : 0,
                    usados, Math.max(0, a.getLimiteIntentos() - usados), a.getLimiteIntentos());
        }).toList();
        List<ReinicioProgreso> reinicios = reinicioRepository.findByEstudianteIdOrderByFechaDesc(usuarioId);
        return new AvanceDTO(objetivos, reinicios.isEmpty() ? null : reinicios.get(0).getFecha());
    }

    // ------------------------------------------------------------ docente y UTP

    @Transactional(readOnly = true)
    public List<CursoDTO> cursos(Rol rol, Long usuarioId) {
        List<Curso> cursos = rol == Rol.UTP
                ? cursoRepository.findAllByOrderByNombreAsc()
                : cursoRepository.findByDocenteIdOrderByNombreAsc(usuarioId);
        return cursos.stream().map(c -> new CursoDTO(c.getId(), c.getNombre(), c.getDocenteId())).toList();
    }

    /** Estudiantes del curso con su avance global y las dos fechas que ayudan a interpretar la inactividad. */
    @Transactional(readOnly = true)
    public List<EstudianteCursoDTO> estudiantesDelCurso(Rol rol, Long usuarioId, Long cursoId) {
        accesoService.cursoPermitido(cursoId, rol, usuarioId);
        List<Estudiante> estudiantes = estudianteRepository.findByCursoIdOrderByNombreAsc(cursoId);
        if (estudiantes.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Progreso>> progresos = progresoRepository
                .findByEstudianteIdIn(estudiantes.stream().map(Estudiante::getUsuarioId).toList()).stream()
                .collect(Collectors.groupingBy(Progreso::getEstudianteId));
        return estudiantes.stream().map(e -> {
            List<Integer> porcentajes = progresos.getOrDefault(e.getUsuarioId(), List.of()).stream()
                    .map(Progreso::getPorcentajeAvance).toList();
            return new EstudianteCursoDTO(e.getUsuarioId(), e.getNombre(), ReglasProgreso.promedio(porcentajes),
                    e.getUltimaConexion(), e.getUltimaSincronizacion());
        }).toList();
    }

    private Estudiante estudiante(Long usuarioId) {
        return estudianteRepository.findById(usuarioId)
                .orElseThrow(() -> new ForbiddenException("Tu usuario no está registrado como estudiante"));
    }
}
