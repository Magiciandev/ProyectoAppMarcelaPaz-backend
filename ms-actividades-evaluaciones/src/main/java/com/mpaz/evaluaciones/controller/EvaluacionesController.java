package com.mpaz.evaluaciones.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mpaz.evaluaciones.dto.ActividadDTO;
import com.mpaz.evaluaciones.dto.ActividadRequest;
import com.mpaz.evaluaciones.dto.EstadoRequest;
import com.mpaz.evaluaciones.dto.PaqueteDTO;
import com.mpaz.evaluaciones.dto.PreguntaDTO;
import com.mpaz.evaluaciones.dto.PreguntaEnActividadDTO;
import com.mpaz.evaluaciones.dto.PreguntaEnPruebaDTO;
import com.mpaz.evaluaciones.dto.PreguntaRequest;
import com.mpaz.evaluaciones.dto.PruebaDTO;
import com.mpaz.evaluaciones.dto.PruebaRequest;
import com.mpaz.evaluaciones.seguridad.Sesion;
import com.mpaz.evaluaciones.service.ActividadService;
import com.mpaz.evaluaciones.service.CascadaEstadoService;
import com.mpaz.evaluaciones.service.PaqueteService;
import com.mpaz.evaluaciones.service.PreguntaService;
import com.mpaz.evaluaciones.service.PruebaService;

import jakarta.validation.Valid;

/**
 * Quién puede escribir y quién puede leer el banco de preguntas se decide en SecurityConfig (solo UTP).
 * Qué ve cada rol en actividades, pruebas y paquete se decide en los servicios, según el estado.
 */
@RestController
@RequestMapping("/evaluaciones")
public class EvaluacionesController {

    private final PreguntaService preguntaService;
    private final ActividadService actividadService;
    private final PruebaService pruebaService;
    private final PaqueteService paqueteService;
    private final CascadaEstadoService cascadaEstadoService;

    public EvaluacionesController(PreguntaService preguntaService, ActividadService actividadService,
                                  PruebaService pruebaService, PaqueteService paqueteService,
                                  CascadaEstadoService cascadaEstadoService) {
        this.preguntaService = preguntaService;
        this.actividadService = actividadService;
        this.pruebaService = pruebaService;
        this.paqueteService = paqueteService;
        this.cascadaEstadoService = cascadaEstadoService;
    }

    // ------------------------------------------------------------ preguntas (UTP)

    @GetMapping("/preguntas")
    public ResponseEntity<List<PreguntaDTO>> preguntas(@RequestParam(name = "objetivoId") Long objetivoId) {
        return ResponseEntity.ok(preguntaService.listar(objetivoId));
    }

    @PostMapping("/preguntas")
    public ResponseEntity<PreguntaDTO> crearPregunta(@Valid @RequestBody PreguntaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(preguntaService.crear(request));
    }

    @PutMapping("/preguntas/{id}")
    public ResponseEntity<PreguntaDTO> actualizarPregunta(@PathVariable("id") Long id,
                                                          @Valid @RequestBody PreguntaRequest request) {
        return ResponseEntity.ok(preguntaService.actualizar(id, request));
    }

    @DeleteMapping("/preguntas/{id}")
    public ResponseEntity<Void> eliminarPregunta(@PathVariable("id") Long id) {
        preguntaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------ actividades

    @GetMapping("/actividades")
    public ResponseEntity<List<ActividadDTO>> actividades(@AuthenticationPrincipal Jwt jwt,
                                                          @RequestParam(name = "objetivoId") Long objetivoId) {
        return ResponseEntity.ok(actividadService.listar(Sesion.rol(jwt), objetivoId));
    }

    @PostMapping("/actividades")
    public ResponseEntity<ActividadDTO> crearActividad(@Valid @RequestBody ActividadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actividadService.crear(request));
    }

    @PutMapping("/actividades/{id}")
    public ResponseEntity<ActividadDTO> actualizarActividad(@PathVariable("id") Long id,
                                                            @Valid @RequestBody ActividadRequest request) {
        return ResponseEntity.ok(actividadService.actualizar(id, request));
    }

    @PutMapping("/actividades/{id}/preguntas")
    public ResponseEntity<List<PreguntaEnActividadDTO>> preguntasDeActividad(
            @PathVariable("id") Long id, @Valid @RequestBody List<PreguntaEnActividadDTO> items) {
        return ResponseEntity.ok(actividadService.reemplazarPreguntas(id, items));
    }

    // ------------------------------------------------------------ pruebas finales

    @GetMapping("/pruebas")
    public ResponseEntity<PruebaDTO> prueba(@AuthenticationPrincipal Jwt jwt,
                                            @RequestParam(name = "objetivoId") Long objetivoId) {
        return ResponseEntity.ok(pruebaService.obtenerPorObjetivo(Sesion.rol(jwt), objetivoId));
    }

    @PostMapping("/pruebas")
    public ResponseEntity<PruebaDTO> crearPrueba(@Valid @RequestBody PruebaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pruebaService.crear(request));
    }

    @PutMapping("/pruebas/{id}")
    public ResponseEntity<PruebaDTO> actualizarPrueba(@PathVariable("id") Long id,
                                                      @Valid @RequestBody PruebaRequest request) {
        return ResponseEntity.ok(pruebaService.actualizar(id, request));
    }

    @PutMapping("/pruebas/{id}/preguntas")
    public ResponseEntity<List<PreguntaEnPruebaDTO>> preguntasDePrueba(
            @PathVariable("id") Long id, @Valid @RequestBody List<PreguntaEnPruebaDTO> items) {
        return ResponseEntity.ok(pruebaService.reemplazarPreguntas(id, items));
    }

    // ------------------------------------------------------------ paquete para la caché de la app

    @GetMapping("/objetivos/{id}/paquete")
    public ResponseEntity<PaqueteDTO> paquete(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id) {
        return ResponseEntity.ok(paqueteService.paquete(Sesion.rol(jwt), id));
    }

    // ------------------------------------------------------------ ruta interna (solo ms-admin)

    @PutMapping("/interno/objetivos/{id}/estado")
    public ResponseEntity<Void> fijarEstado(@PathVariable("id") Long id,
                                            @Valid @RequestBody EstadoRequest request) {
        cascadaEstadoService.fijarEstado(id, request.getEstado());
        return ResponseEntity.noContent().build();
    }
}
