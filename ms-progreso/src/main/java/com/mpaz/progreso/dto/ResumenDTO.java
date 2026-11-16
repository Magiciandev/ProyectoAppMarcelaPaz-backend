package com.mpaz.progreso.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDTO {
    private Long estudianteId;
    private String nombre;
    private List<ResumenObjetivoDTO> objetivos;
    private Integer promedioGeneral;
    private String nivelGeneral;
}
