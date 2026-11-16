package com.mpaz.progreso.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstudianteCursoDTO {
    private Long usuarioId;
    private String nombre;
    private Integer avanceGlobal;
    private Instant ultimaConexion;
    private Instant ultimaSincronizacion;
}
