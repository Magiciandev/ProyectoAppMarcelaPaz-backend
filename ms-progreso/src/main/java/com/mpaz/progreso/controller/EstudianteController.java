package com.mpaz.progreso.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mpaz.progreso.dto.AsignacionEstudianteDTO;
import com.mpaz.progreso.dto.AvanceDTO;
import com.mpaz.progreso.dto.EstudianteDTO;
import com.mpaz.progreso.dto.SyncRequest;
import com.mpaz.progreso.dto.SyncResponse;
import com.mpaz.progreso.seguridad.Sesion;
import com.mpaz.progreso.service.EstudianteService;
import com.mpaz.progreso.service.SincronizacionService;

/** Lo que usa la app del estudiante. Solo el rol ESTUDIANTE llega aquí (ver SecurityConfig). */
@RestController
@RequestMapping("/progreso")
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final SincronizacionService sincronizacionService;

    public EstudianteController(EstudianteService estudianteService, SincronizacionService sincronizacionService) {
        this.estudianteService = estudianteService;
        this.sincronizacionService = sincronizacionService;
    }

    @GetMapping("/estudiantes/me")
    public ResponseEntity<EstudianteDTO> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(estudianteService.me(Sesion.usuarioId(jwt)));
    }

    @GetMapping("/estudiantes/me/asignaciones")
    public ResponseEntity<List<AsignacionEstudianteDTO>> asignaciones(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(estudianteService.asignacionesVigentes(Sesion.usuarioId(jwt)));
    }

    @GetMapping("/estudiantes/me/avance")
    public ResponseEntity<AvanceDTO> avance(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(estudianteService.avance(Sesion.usuarioId(jwt)));
    }

    @PostMapping("/conexion")
    public ResponseEntity<Void> conexion(@AuthenticationPrincipal Jwt jwt) {
        estudianteService.registrarConexion(Sesion.usuarioId(jwt));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> sync(@AuthenticationPrincipal Jwt jwt, @RequestBody SyncRequest request) {
        return ResponseEntity.ok(sincronizacionService.sincronizar(Sesion.usuarioId(jwt), request));
    }
}
