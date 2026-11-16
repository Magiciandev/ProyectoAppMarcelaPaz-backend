package com.mpaz.progreso.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mpaz.progreso.dto.AsignacionDTO;
import com.mpaz.progreso.dto.AsignacionRequest;
import com.mpaz.progreso.dto.CursoDTO;
import com.mpaz.progreso.dto.EstudianteCursoDTO;
import com.mpaz.progreso.dto.InformeDTO;
import com.mpaz.progreso.dto.InformeRequest;
import com.mpaz.progreso.dto.IntentoDTO;
import com.mpaz.progreso.dto.ReinicioDTO;
import com.mpaz.progreso.dto.ReinicioRequest;
import com.mpaz.progreso.dto.ResumenDTO;
import com.mpaz.progreso.dto.ValidacionRequest;
import com.mpaz.progreso.model.Estudiante;
import com.mpaz.progreso.model.Rol;
import com.mpaz.progreso.seguridad.Sesion;
import com.mpaz.progreso.service.AccesoService;
import com.mpaz.progreso.service.AsignacionService;
import com.mpaz.progreso.service.EstudianteService;
import com.mpaz.progreso.service.InformeService;
import com.mpaz.progreso.service.ReinicioService;
import com.mpaz.progreso.service.ResumenService;

import jakarta.validation.Valid;

/**
 * Monitoreo, asignaciones, informes y reinicio. Docente y UTP pueden consultar; solo el docente escribe
 * (ver SecurityConfig). Un docente solo ve y modifica sus propios cursos (ver AccesoService).
 */
@RestController
@RequestMapping("/progreso")
public class SeguimientoController {

    private final EstudianteService estudianteService;
    private final AsignacionService asignacionService;
    private final ResumenService resumenService;
    private final InformeService informeService;
    private final ReinicioService reinicioService;
    private final AccesoService accesoService;

    public SeguimientoController(EstudianteService estudianteService, AsignacionService asignacionService,
                                 ResumenService resumenService, InformeService informeService,
                                 ReinicioService reinicioService, AccesoService accesoService) {
        this.estudianteService = estudianteService;
        this.asignacionService = asignacionService;
        this.resumenService = resumenService;
        this.informeService = informeService;
        this.reinicioService = reinicioService;
        this.accesoService = accesoService;
    }

    // ------------------------------------------------------------ cursos y estudiantes

    @GetMapping("/cursos")
    public ResponseEntity<List<CursoDTO>> cursos(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(estudianteService.cursos(Sesion.rol(jwt), Sesion.usuarioId(jwt)));
    }

    @GetMapping("/cursos/{cursoId}/estudiantes")
    public ResponseEntity<List<EstudianteCursoDTO>> estudiantes(@AuthenticationPrincipal Jwt jwt,
                                                                @PathVariable("cursoId") Long cursoId) {
        return ResponseEntity.ok(estudianteService.estudiantesDelCurso(Sesion.rol(jwt), Sesion.usuarioId(jwt), cursoId));
    }

    // ------------------------------------------------------------ asignaciones

    @PostMapping("/asignaciones")
    public ResponseEntity<AsignacionDTO> crearAsignacion(@AuthenticationPrincipal Jwt jwt,
                                                         @Valid @RequestBody AsignacionRequest request) {
        // Se reenvía el token del docente a ms-contenidos para comprobar que el objetivo le es visible.
        String authorization = "Bearer " + jwt.getTokenValue();
        AsignacionDTO creada = asignacionService.crear(Sesion.usuarioId(jwt), request, authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/asignaciones")
    public ResponseEntity<List<AsignacionDTO>> asignaciones(@AuthenticationPrincipal Jwt jwt,
                                                            @RequestParam(name = "cursoId") Long cursoId) {
        return ResponseEntity.ok(asignacionService.listarPorCurso(Sesion.rol(jwt), Sesion.usuarioId(jwt), cursoId));
    }

    @PutMapping("/asignaciones/{id}")
    public ResponseEntity<AsignacionDTO> actualizarAsignacion(@AuthenticationPrincipal Jwt jwt,
                                                              @PathVariable("id") Long id,
                                                              @Valid @RequestBody AsignacionRequest request) {
        return ResponseEntity.ok(asignacionService.actualizar(id, Sesion.usuarioId(jwt), request));
    }

    // ------------------------------------------------------------ monitoreo de un estudiante

    @GetMapping("/estudiantes/{id}/resumen")
    public ResponseEntity<ResumenDTO> resumen(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id) {
        Estudiante e = accesoService.estudiantePermitido(id, Sesion.rol(jwt), Sesion.usuarioId(jwt));
        return ResponseEntity.ok(resumenService.resumen(e));
    }

    @GetMapping("/estudiantes/{id}/intentos")
    public ResponseEntity<List<IntentoDTO>> intentos(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id,
                                                     @RequestParam(name = "objetivoId", required = false) Long objetivoId) {
        Estudiante e = accesoService.estudiantePermitido(id, Sesion.rol(jwt), Sesion.usuarioId(jwt));
        return ResponseEntity.ok(resumenService.intentos(e, objetivoId));
    }

    @PostMapping("/estudiantes/{id}/reinicio")
    public ResponseEntity<ReinicioDTO> reinicio(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id,
                                                @Valid @RequestBody ReinicioRequest request) {
        return ResponseEntity.ok(reinicioService.reiniciar(Sesion.usuarioId(jwt), id, request));
    }

    // ------------------------------------------------------------ informes

    @PostMapping("/informes")
    public ResponseEntity<InformeDTO> generarInforme(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody InformeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(informeService.generar(Sesion.usuarioId(jwt), request));
    }

    @GetMapping("/informes")
    public ResponseEntity<List<InformeDTO>> informes(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestParam(name = "cursoId", required = false) Long cursoId,
                                                     @RequestParam(name = "estudianteId", required = false) Long estudianteId) {
        Rol rol = Sesion.rol(jwt);
        return ResponseEntity.ok(informeService.listar(rol, Sesion.usuarioId(jwt), cursoId, estudianteId));
    }

    @GetMapping("/informes/{id}")
    public ResponseEntity<InformeDTO> informe(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id) {
        return ResponseEntity.ok(informeService.obtener(Sesion.rol(jwt), Sesion.usuarioId(jwt), id));
    }

    @PutMapping("/informes/{id}/validacion")
    public ResponseEntity<InformeDTO> validar(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id,
                                              @Valid @RequestBody ValidacionRequest request) {
        return ResponseEntity.ok(informeService.validar(id, Sesion.usuarioId(jwt), request));
    }
}
