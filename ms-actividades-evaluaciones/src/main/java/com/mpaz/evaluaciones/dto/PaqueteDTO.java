package com.mpaz.evaluaciones.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaqueteDTO {
    private Long objetivoId;
    private List<ActividadDTO> actividades;
    private List<PreguntaDTO> preguntas;
    private PruebaDTO prueba;
}
