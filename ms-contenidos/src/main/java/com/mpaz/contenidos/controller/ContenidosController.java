package com.mpaz.contenidos.controller;

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

import com.mpaz.contenidos.dto.AsignaturaDTO;
import com.mpaz.contenidos.dto.ContenidoDTO;
import com.mpaz.contenidos.dto.ContenidoRequest;
import com.mpaz.contenidos.dto.EstadoRequest;
import com.mpaz.contenidos.dto.ObjetivoDTO;
import com.mpaz.contenidos.dto.ObjetivoRequest;
import com.mpaz.contenidos.dto.PaqueteDTO;
import com.mpaz.contenidos.dto.UnidadDTO;
import com.mpaz.contenidos.dto.UnidadRequest;
import com.mpaz.contenidos.seguridad.Sesion;
import com.mpaz.contenidos.service.ContenidosService;

import jakarta.validation.Valid;

/**
 * Quién puede escribir (POST, PUT, DELETE) se decide en SecurityConfig: solo UTP.
 * Qué puede ver cada rol en las lecturas se decide en el servicio, según el estado.
 */
@RestController
@RequestMapping("/contenidos")
public class ContenidosController {

    private final ContenidosService service;

    public ContenidosController(ContenidosService service) {
        this.service = service;
    }

    // ------------------------------------------------------------ lecturas

    @GetMapping("/asignaturas")
    public ResponseEntity<List<AsignaturaDTO>> asignaturas() {
        return ResponseEntity.ok(service.listarAsignaturas());
    }

    @GetMapping("/unidades")
    public ResponseEntity<List<UnidadDTO>> unidades(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "asignaturaId", required = false) Long asignaturaId) {
        return ResponseEntity.ok(service.listarUnidades(Sesion.rol(jwt), asignaturaId));
    }

    @GetMapping("/objetivos")
    public ResponseEntity<List<ObjetivoDTO>> objetivos(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "unidadId", required = false) Long unidadId,
            @RequestParam(name = "ids", required = false) List<Long> ids) {
        return ResponseEntity.ok(service.listarObjetivos(Sesion.rol(jwt), unidadId, ids));
    }

    @GetMapping("/objetivos/{id}")
    public ResponseEntity<ObjetivoDTO> objetivo(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id) {
        return ResponseEntity.ok(service.obtenerObjetivo(Sesion.rol(jwt), id));
    }

    @GetMapping("/objetivos/{id}/paquete")
    public ResponseEntity<PaqueteDTO> paquete(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id) {
        return ResponseEntity.ok(service.paquete(Sesion.rol(jwt), id));
    }

    // ------------------------------------------------------------ escrituras (UTP)

    @PostMapping("/unidades")
    public ResponseEntity<UnidadDTO> crearUnidad(@AuthenticationPrincipal Jwt jwt,
                                                 @Valid @RequestBody UnidadRequest request) {
        UnidadDTO creada = service.crearUnidad(request, Sesion.usuarioId(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/unidades/{id}")
    public ResponseEntity<UnidadDTO> actualizarUnidad(@PathVariable("id") Long id,
                                                      @Valid @RequestBody UnidadRequest request) {
        return ResponseEntity.ok(service.actualizarUnidad(id, request));
    }

    @PostMapping("/unidades/{unidadId}/objetivos")
    public ResponseEntity<ObjetivoDTO> crearObjetivo(@PathVariable("unidadId") Long unidadId,
                                                     @Valid @RequestBody ObjetivoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearObjetivo(unidadId, request));
    }

    @PutMapping("/objetivos/{id}")
    public ResponseEntity<ObjetivoDTO> actualizarObjetivo(@PathVariable("id") Long id,
                                                          @Valid @RequestBody ObjetivoRequest request) {
        return ResponseEntity.ok(service.actualizarObjetivo(id, request));
    }

    @PostMapping("/objetivos/{id}/contenidos")
    public ResponseEntity<ContenidoDTO> crearContenido(@PathVariable("id") Long id,
                                                       @Valid @RequestBody ContenidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearContenido(id, request));
    }

    @PutMapping("/contenidos/{id}")
    public ResponseEntity<ContenidoDTO> actualizarContenido(@PathVariable("id") Long id,
                                                            @Valid @RequestBody ContenidoRequest request) {
        return ResponseEntity.ok(service.actualizarContenido(id, request));
    }

    @DeleteMapping("/contenidos/{id}")
    public ResponseEntity<Void> eliminarContenido(@PathVariable("id") Long id) {
        service.eliminarContenido(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------ ruta interna (solo ms-admin)

    @PutMapping("/interno/objetivos/{id}/estado")
    public ResponseEntity<ObjetivoDTO> fijarEstado(@PathVariable("id") Long id,
                                                   @Valid @RequestBody EstadoRequest request) {
        return ResponseEntity.ok(service.fijarEstadoObjetivo(id, request.getEstado()));
    }
}
