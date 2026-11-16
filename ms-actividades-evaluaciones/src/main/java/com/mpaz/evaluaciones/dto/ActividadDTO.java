package com.mpaz.evaluaciones.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mpaz.evaluaciones.model.ActividadDesafio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDTO {
    private Long id;
    private Long objetivoId;
    private String titulo;
    private String tipo;
    private Integer dificultad;
    private Integer orden;
    private String estado;
    /** Solo viene en el paquete; en los listados es null. */
    private List<PreguntaEnActividadDTO> preguntas;

    public static ActividadDTO fromModel(ActividadDesafio a, List<PreguntaEnActividadDTO> preguntas) {
        return new ActividadDTO(a.getId(), a.getObjetivoAprendizajeId(), a.getTitulo(), a.getTipo(),
                a.getDificultad(), a.getOrden(), a.getEstado(), preguntas);
    }
}
