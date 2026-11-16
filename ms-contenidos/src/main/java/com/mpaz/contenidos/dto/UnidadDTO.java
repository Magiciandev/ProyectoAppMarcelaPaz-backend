package com.mpaz.contenidos.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.mpaz.contenidos.model.Unidad;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnidadDTO {
    private Long id;
    private Long asignaturaId;
    private String titulo;
    private Integer orden;
    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long creadoPor;
    private Instant creadoEn;

    public static UnidadDTO fromModel(Unidad u) {
        if (u == null) return null;
        return new UnidadDTO(u.getId(), u.getAsignaturaId(), u.getTitulo(), u.getOrden(), u.getEstado(),
                u.getFechaInicio(), u.getFechaFin(), u.getCreadoPor(), u.getCreadoEn());
    }
}
