package com.mpaz.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mpaz.admin.dto.AprobacionRequest;
import com.mpaz.admin.dto.AprobacionResponse;
import com.mpaz.admin.dto.HistorialDTO;
import com.mpaz.admin.seguridad.Sesion;
import com.mpaz.admin.service.AprobacionService;

import jakarta.validation.Valid;

/** Todo lo de /admin es solo para el perfil UTP (ver SecurityConfig). */
@RestController
@RequestMapping("/admin")
public class AprobacionController {

    private final AprobacionService service;

    public AprobacionController(AprobacionService service) {
        this.service = service;
    }

    @PostMapping("/aprobaciones")
    public ResponseEntity<AprobacionResponse> aprobar(@AuthenticationPrincipal Jwt jwt,
                                                      @Valid @RequestBody AprobacionRequest request) {
        // Se reenvía el mismo token del usuario UTP a los demás servicios.
        String authorization = "Bearer " + jwt.getTokenValue();
        return ResponseEntity.ok(service.aplicar(request, Sesion.usuarioId(jwt), authorization));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<HistorialDTO>> historial(
            @RequestParam(name = "entidad", defaultValue = "OBJETIVO") String entidad,
            @RequestParam(name = "entidadId") Long entidadId) {
        return ResponseEntity.ok(service.historial(entidad, entidadId));
    }
}
