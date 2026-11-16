package com.mpaz.progreso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstudianteDTO {
    private Long usuarioId;
    private String nombre;
    private Long cursoId;
    private String cursoNombre;
}
