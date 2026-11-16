package com.mpaz.progreso.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mpaz.progreso.model.Asignacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionDTO {
    private Long id;
    private Long objetivoId;
    private Long cursoId;
    private Long estudianteId;
    private Long docenteId;
    private boolean disponible;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Integer limiteIntentos;
    private LocalDate fechaHabilitacion;
    private LocalDate fechaRendicion;

    public static AsignacionDTO fromModel(Asignacion a) {
        return new AsignacionDTO(a.getId(), a.getObjetivoAprendizajeId(), a.getCursoId(), a.getEstudianteId(), a.getDocenteId(),
                a.isDisponible(), a.getFechaDesde(), a.getFechaHasta(), a.getLimiteIntentos(), a.getFechaHabilitacion(),
                a.getFechaRendicion());
    }
}
